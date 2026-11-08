import http from "node:http";

const port = Number(process.env.PORT || 8080);
const host = process.env.HOST || "0.0.0.0";
const model = process.env.OPENAI_MODEL || "gpt-4o-mini";
const apiKey = process.env.OPENAI_API_KEY;
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
  if (!apiKey) throw new Error("OPENAI_API_KEY is not configured on the server");
  const messages = Array.isArray(body.messages) ? body.messages.slice(-20) : [];
  if (!messages.length || !messages.some((item) => item.role === "user" && String(item.content || "").trim())) {
    throw new Error("At least one user message is required");
  }
  const context = String(body.projectContext || "").slice(0, 20_000);
  const system = {
    role: "system",
    content: "You are CursorAi, a careful coding assistant. Give actionable plans, explain assumptions, and prefer small reviewable changes. Never claim that code was run unless tool output is provided. Project context:\n" + context,
  };
  const upstream = await fetch("https://api.openai.com/v1/chat/completions", {
    method: "POST",
    headers: { Authorization: `Bearer ${apiKey}`, "Content-Type": "application/json" },
    body: JSON.stringify({ model, messages: [system, ...messages], temperature: 0.2, max_tokens: 1200 }),
  });
  const data = await upstream.json();
  if (!upstream.ok) throw new Error(data?.error?.message || `OpenAI request failed (${upstream.status})`);
  return { model, message: data.choices?.[0]?.message?.content || "No response returned" };
}

const server = http.createServer(async (req, res) => {
  try {
    if (req.method === "OPTIONS") return send(res, 204, {});
    if (req.method === "GET" && req.url === "/health") return send(res, 200, { ok: true, provider: "openai", model });
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
