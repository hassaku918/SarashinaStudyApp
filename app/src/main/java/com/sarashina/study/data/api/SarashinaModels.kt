package com.sarashina.study.data.api

import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(
    val model: String = "sarashina3.2-mini-thinking",
    val messages: List<Message>,
    @SerializedName("max_completion_tokens")
    val maxCompletionTokens: Int = 4000,
    val temperature: Double = 0.7,
    @SerializedName("reasoning_effort")
    val reasoningEffort: String? = "high",
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String?,
    val reasoning: String? = null
)

data class ChatCompletionResponse(
    val id: String?,
    val `object`: String?,
    val created: Long?,
    val model: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int?,
    val message: Message?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?
)

data class ApiError(val error: ErrorDetail?)
data class ErrorDetail(val message: String?, val type: String?, val code: String?)
