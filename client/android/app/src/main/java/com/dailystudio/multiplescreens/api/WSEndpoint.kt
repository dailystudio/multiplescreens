package com.dailystudio.multiplescreens.api

import com.dailystudio.devbricksx.development.Logger
import com.google.gson.Gson
import okhttp3.*
import java.util.concurrent.TimeUnit

interface WSEndpointListener {

    fun onConnected(endpoint: WSEndpoint)
    fun onDisconnect(endpoint: WSEndpoint, code: Int)
    fun onCommand(endpoint: WSEndpoint, command: Command)

}

class WSEndpoint(private val sid: String,
                 private val uuid: String,
                 private val listener: WSEndpointListener? = null) {

    companion object {
//        private const val WS_URL = "ws://192.168.28.118:1809/screen"
//        private const val WS_URL = "ws://192.168.2.237:1809/screen"
        private const val WS_URL = "wss://multiplescreens.orangelabschina.cn:1809/screen"
        private const val CLOSE_REASON = "normal_close"

        const val CODE_CLOSE_NORMAL = 4000
        const val CODE_CLOSE_FAILED = 4001

        private val GSON = Gson()
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

                listener?.onConnected(this@WSEndpoint)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Logger.error("ws failed: t = ${t.printStackTrace()}, resp = $response")

                super.onFailure(webSocket, t, response)

                wsSocket = null

                listener?.onDisconnect(this@WSEndpoint, CODE_CLOSE_FAILED)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Logger.debug("ws is closing: code = $code, reason = $reason")
                super.onClosing(webSocket, code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Logger.debug("ws closed: code = $code, reason = $reason")

                super.onClosed(webSocket, code, reason)

                wsSocket = null

                listener?.onDisconnect(this@WSEndpoint, code)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Logger.debug("ws text: %s", text)

                val cmdObject = GSON.fromJson(text, Command::class.java)
                when (cmdObject.cmdCode) {
                    CmdCode.UPDATE_SCREEN_INFO -> {
                        listener?.onCommand(this@WSEndpoint,
                            GSON.fromJson(text, CmdUpdateScreenInfo::class.java))
                    }

                    CmdCode.SYNC_GRIDS_MAP -> {
                        listener?.onCommand(this@WSEndpoint,
                            GSON.fromJson(text, CmdGridsMap::class.java))
                    }

                    CmdCode.DRAW_POINT -> {
                        listener?.onCommand(this@WSEndpoint,
                            GSON.fromJson(text, CmdDrawPoint::class.java))
                    }

                    else -> {
                        Logger.warn("unsupported cmd: $cmdObject")
                    }
                }
            }

        })
    }

    fun disconnect() {
        wsSocket?.close(CODE_CLOSE_NORMAL, CLOSE_REASON)
    }

    fun reportScreenInfo(widthInDp: Int, heightInDp: Int) {
        val cmd = CmdReportScreenInfo(uuid, widthInDp , heightInDp);

        wsSocket?.send(GSON.toJson(cmd))
    }

    fun startDrawing() {
        val cmd = CmdStartDrawing(uuid, sid);

        wsSocket?.send(GSON.toJson(cmd))
    }

    fun stopDrawing() {
        val cmd = CmdStopDrawing(uuid, sid);

        wsSocket?.send(GSON.toJson(cmd))
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