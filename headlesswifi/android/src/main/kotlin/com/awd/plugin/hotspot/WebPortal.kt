package com.awd.plugin.hotspot

import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class WebPortal(
    private val listener: WebPortListener
): NanoHTTPD(8080) {
    interface WebPortListener {
        fun onCredentialsSubmit(ssid: String, password: String, isHiddenNetwork: Boolean, postCallback: PostCallback)
    }

    interface PostCallback {
        fun onComplete(connected: Boolean, hasInternet: Boolean)
    }
    
    override fun serve(session: IHTTPSession?): Response? {
        return runBlocking {
            when(session?.method) {
                Method.GET -> {
                    val html = """
                        <html>
                            <body>
                                <form method="POST">
                                    SSID: <input type="text" name="ssid"><br>
                                    Password: <input type="password" name="password"><br>
                                    Hidden Network: <input type="checkbox" name="hidden_checkbox"><br>
                                    <input type="submit" value="Submit">
                                </form>
                            </body>
                        </html>
                    """.trimIndent()
                    newFixedLengthResponse(Response.Status.OK, "text/html", html).apply {
                        addHeader("Access-Control-Allow-Origin", "*")
                        addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                        addHeader("Access-Control-Allow-Headers", "Content-Type")
                    }
                }
                Method.POST -> {
                    var params = mutableMapOf<String, String>()
                    session.parseBody(params)
                    println("session.parms: ${session.parms}")
                    println("session body: $params")
                    params = if (params.isEmpty()) session.parms else params
                    val ssid = params["ssid"] ?: ""
                    val password = params["password"] ?: ""
                    val hidden = params["hidden_checkbox"] == "on"
                    val connectionCompleter = CompletableDeferred<Pair<Boolean, Boolean>>()

                    listener.onCredentialsSubmit(ssid, password, hidden, object: PostCallback {
                        override fun onComplete(connected: Boolean, hasInternet: Boolean) {
                            println("onComplete($connected, $hasInternet)")
                            connectionCompleter.complete(Pair(connected, hasInternet))
                        }
                    })

                    val (connected, _) = connectionCompleter.await()

                    if(!connected) {
                        val html = """
                        <html>
                            <body>
                                There was an error connecting to the wifi. Try to reconnect.
                                <form method="POST">
                                    SSID: <input type="text" name="ssid"><br>
                                    Password: <input type="password" name="password"><br>
                                    Hidden Network: <input type="checkbox" name="hidden_checkbox"><br>
                                    <input type="submit" value="Submit">
                                </form>
                            </body>
                        </html>
                    """.trimIndent()
                        return@runBlocking  newFixedLengthResponse(Response.Status.OK, "text/html", html).apply {
                            addHeader("Access-Control-Allow-Origin", "*")
                            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                            addHeader("Access-Control-Allow-Headers", "Content-Type")
                        }
                    }
                    newFixedLengthResponse(Response.Status.OK, "text/html", "&#9989 Connected").apply {
                        addHeader("Access-Control-Allow-Origin", "*")
                        addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                        addHeader("Access-Control-Allow-Headers", "Content-Type")
                    }
                }
                else -> newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed")
            }
        }
    }
}