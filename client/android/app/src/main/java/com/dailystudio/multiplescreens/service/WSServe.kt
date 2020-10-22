package com.dailystudio.multiplescreens.service

import com.dailystudio.devbricksx.development.Logger
import okhttp3.*
import java.util.concurrent.TimeUnit

class WSServe(val sid: String, val uuid: String) {

    companion object {
        private const val WS_URL = "ws://192.168.28.253:1809/screen"
        private const val CLOSE_REASON = "normal_close"

        private const val CODE_CLOSE_NORMAL = 4000
        private const val CODE_CLOSE_BY_SERVER = 4001
    }

    private var wsSocket: WebSocket? = null

    fun connect() {
        val client = OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true }
            .readTimeout(3, TimeUnit.SECONDS)
            .build();

        val requestUrl = buildUrl(WS_URL, sid, uuid)
        Logger.debug("ws url: $requestUrl")
        val request: Request = Request.Builder()
            .url(requestUrl)
            .build()

        wsSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Logger.debug("ws connected: resp = $response")
                super.onOpen(webSocket, response)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Logger.error("ws failed: t = $t, resp = $response")

                super.onFailure(webSocket, t, response)

                wsSocket = null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Logger.debug("ws is closing: code = $code, reason = $reason")
                super.onClosing(webSocket, code, reason)
                wsSocket?.close(CODE_CLOSE_BY_SERVER, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Logger.debug("ws closed: code = $code, reason = $reason")

                super.onClosed(webSocket, code, reason)

                wsSocket = null
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Logger.debug("ws text: %s", text)
            }

        })
    }

    fun disconnect() {
        wsSocket?.close(CODE_CLOSE_NORMAL, CLOSE_REASON)
    }

    private fun buildUrl(baseUrl: String,
                         sid: String,
                         uuid: String): String {
        return buildString {
            append(baseUrl)
            append('?')
            append("sid=")
            append(sid)
            append("&uuid=")
            append(uuid)
        }

    }

}