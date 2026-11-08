package com.cursorai.app.ai

data class AssistantSettings(
    val provider: String = "openai",
    val model: String = "gpt-4o-mini",
    val baseUrl: String = "",
) {
    val displayProvider: String
        get() = when (provider) {
            "anthropic" -> "Claude"
            "self-hosted" -> "Self-hosted"
            else -> "OpenAI"
        }
}
