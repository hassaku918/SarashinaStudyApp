package com.sarashina.study.util

import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException

object ApiErrorMapper {

    fun toUserMessage(e: Throwable?): String {
        if (e == null) return "不明なエラーが発生しました"
        if (e is CancellationException) return "処理がキャンセルされました"
        val msg = e.message.orEmpty()
        return when {
            e is SocketTimeoutException || msg.contains("timeout", true) || msg.contains("timed out", true) ->
                "タイムアウトしました。推論が長い、または通信が遅い可能性があります。推論の深さを medium/low にするか、再試行してください。\n詳細: $msg"
            e is UnknownHostException || msg.contains("Unable to resolve host", true) ->
                "ネットワークに接続できません。Wi-Fi / モバイルデータと機内モードを確認してください。"
            msg.contains("API Error 401") || msg.contains("Unauthorized", true) ->
                "APIキーが無効です。設定で Sarashina API Key を確認してください。"
            msg.contains("API Error 403") ->
                "アクセスが拒否されました（403）。APIキーの権限を確認してください。"
            msg.contains("API Error 429") ->
                "レート制限に達しました（429）。少し待ってから再試行してください。"
            msg.contains("API Error 500") || msg.contains("API Error 502") || msg.contains("API Error 503") ->
                "Sarashina サーバー側の一時的な障害の可能性があります。しばらくして再試行してください。\n$msg"
            msg.startsWith("API Error") -> {
                val body = msg.substringAfter(": ").trim()
                val parsed = parseApiBody(body)
                "APIエラー: $parsed"
            }
            msg.isBlank() -> "エラーが発生しました（${e.javaClass.simpleName}）"
            else -> msg
        }
    }

    private fun parseApiBody(body: String): String {
        return try {
            val json = JSONObject(body)
            val err = json.optJSONObject("error")
            val m = err?.optString("message")?.takeIf { it.isNotBlank() }
                ?: json.optString("message").takeIf { it.isNotBlank() }
            val type = err?.optString("type")
            val code = err?.optString("code")
            buildString {
                append(m ?: body.take(300))
                if (!type.isNullOrBlank()) append("（type=$type）")
                if (!code.isNullOrBlank()) append("（code=$code）")
            }
        } catch (_: Exception) {
            body.take(400)
        }
    }
}
