package com.cursorai.app.ai

import android.os.Build
import com.cursorai.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AiAssistant(
    private val baseUrl: String = BuildConfig.CURSORAI_API_BASE_URL,
    private val clientToken: String = BuildConfig.CURSORAI_CLIENT_TOKEN,
) {
    fun ask(prompt: String, projectContext: String = "CursorAi Android project") : Result<String> = runCatching {
        val connection = (URL(baseUrl.trimEnd('/') + "/v1/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            if (clientToken.isNotBlank()) setRequestProperty("X-Client-Token", clientToken)
        }
        val messages = JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
        val body = JSONObject().put("messages", messages).put("projectContext", projectContext).toString()
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val payload = stream.bufferedReader().use { it.readText() }
        val json = JSONObject(payload)
        if (connection.responseCode !in 200..299) error(json.optString("error", "AI request failed"))
        json.optString("message", "No response returned")
    }
}
