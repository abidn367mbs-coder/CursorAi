import http from "node:http";

const port = Number(process.env.PORT || 8080);
const host = process.env.HOST || "0.0.0.0";
const model = process.env.OPENAI_MODEL || "gpt-4o-mini";
const apiKey = process.env.OPENAI_API_KEY;
const anthropicKey = process.env.ANTHROPIC_API_KEY;
const nvidiaKey = process.env.NVIDIA_API_KEY;
const selfHostedBaseUrl = (process.env.SELF_HOSTED_BASE_URL || "").replace(/\/$/, "");
const selfHostedKey = process.env.SELF_HOSTED_API_KEY || "";
const clientToken = process.env.CURSORAI_CLIENT_TOKEN;
const maxBodyBytes = 128 * 1024;
const rateWindowMs = 60_000;
const rateLimit = Number(process.env.RATE_LIMIT_PER_MINUTE || 30);
const requests = new Map();

const headers = {
  "Content-Type": "application/json; charset=utf-8",
  "Access-Control-Allow-Origin": process.env.CORS_ORIGIN || "*",
  "Access-Control-Allow-Headers": "content-type, x-client-token",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

function send(res, status, payload) {
  res.writeHead(status, headers);
  res.end(JSON.stringify(payload));
}

function allowed(req) {
  if (!clientToken) return true;
  return req.headers["x-client-token"] === clientToken;
}

function withinRateLimit(req) {
  const key = req.socket.remoteAddress || "unknown";
  const now = Date.now();
  const current = requests.get(key) || { started: now, count: 0 };
  if (now - current.started >= rateWindowMs) {
    requests.set(key, { started: now, count: 1 });
    return true;
  }
  current.count += 1;
  requests.set(key, current);
  return current.count <= rateLimit;
}

async function readJson(req) {
  let size = 0;
  let raw = "";
  for await (const chunk of req) {
    size += chunk.length;
    if (size > maxBodyBytes) throw new Error("Request body is too large");
    raw += chunk;
  }
  return JSON.parse(raw || "{}");
}

async function chat(body) {
  const provider = ["openai", "anthropic", "nvidia", "self-hosted"].includes(body.provider) ? body.provider : "openai";
  const messages = Array.isArray(body.messages) ? body.messages.slice(-20) : [];
  if (!messages.length || !messages.some((item) => item.role === "user" && String(item.content || "").trim())) {
    throw new Error("At least one user message is required");
  }
  const context = String(body.projectContext || "").slice(0, 20_000);
  const system = {
    role: "system",
    content: "You are CursorAi, a careful coding assistant. Give actionable plans, explain assumptions, and prefer small reviewable changes. Never claim that code was run unless tool output is provided. Project context:\n" + context,
  };
  const selectedModel = String(body.model || "").slice(0, 120) || (provider === "anthropic" ? process.env.ANTHROPIC_MODEL || "claude-3-5-haiku-latest" : provider === "nvidia" ? process.env.NVIDIA_MODEL || "meta/llama-3.1-70b-instruct" : provider === "self-hosted" ? process.env.SELF_HOSTED_MODEL || "your-coding-model" : model);
  if (provider === "anthropic") return anthropicChat(messages, context, selectedModel);
  const baseUrl = provider === "self-hosted" ? selfHostedBaseUrl : provider === "nvidia" ? "https://integrate.api.nvidia.com" : "https://api.openai.com";
  const key = provider === "self-hosted" ? selfHostedKey : provider === "nvidia" ? nvidiaKey : apiKey;
  if (!key && provider !== "self-hosted") throw new Error(`${provider === "nvidia" ? "NVIDIA_API_KEY" : "OPENAI_API_KEY"} is not configured on the server`);
  if (!baseUrl) throw new Error("SELF_HOSTED_BASE_URL is not configured on the server");
  const completionsUrl = baseUrl.endsWith("/v1") ? `${baseUrl}/chat/completions` : `${baseUrl}/v1/chat/completions`;
  const upstreamHeaders = { "Content-Type": "application/json" };
  if (key) upstreamHeaders.Authorization = `Bearer ${key}`;
  const upstream = await fetch(completionsUrl, {
    method: "POST",
    headers: upstreamHeaders,
    body: JSON.stringify({ model: selectedModel, messages: [system, ...messages], temperature: 0.2, max_tokens: 1200 }),
  });
  const data = await upstream.json();
  if (!upstream.ok) throw new Error(data?.error?.message || `OpenAI request failed (${upstream.status})`);
  return { provider, model: selectedModel, message: data.choices?.[0]?.message?.content || "No response returned" };
}

async function anthropicChat(messages, context, selectedModel) {
  if (!anthropicKey) throw new Error("ANTHROPIC_API_KEY is not configured on the server");
  const upstream = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: { "x-api-key": anthropicKey, "anthropic-version": "2023-06-01", "Content-Type": "application/json" },
    body: JSON.stringify({ model: selectedModel, system: "You are CursorAi, a careful coding assistant. Project context:\n" + context, messages, max_tokens: 1200 }),
  });
  const data = await upstream.json();
  if (!upstream.ok) throw new Error(data?.error?.message || `Claude request failed (${upstream.status})`);
  return { provider: "anthropic", model: selectedModel, message: data.content?.map((item) => item.text || "").join("") || "No response returned" };
}

const server = http.createServer(async (req, res) => {
  try {
    if (req.method === "OPTIONS") return send(res, 204, {});
    if (req.method === "GET" && req.url === "/health") return send(res, 200, { ok: true, providers: ["openai", "anthropic", "nvidia", "self-hosted"], model });
    if (req.method !== "POST" || req.url !== "/v1/chat") return send(res, 404, { error: "Not found" });
    if (!allowed(req)) return send(res, 401, { error: "Invalid client token" });
    if (!withinRateLimit(req)) return send(res, 429, { error: "Rate limit exceeded" });
    const body = await readJson(req);
    return send(res, 200, await chat(body));
  } catch (error) {
    console.error(error);
    return send(res, error instanceof SyntaxError ? 400 : 502, { error: error.message || "Server error" });
  }
});

server.listen(port, host, () => console.log(`CursorAi API listening on ${host}:${port}`));
