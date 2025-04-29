package com.awd.plugin.hotspot

import fi.iki.elonen.NanoHTTPD

class WebPortal(
    private val listener: WebPortListener
): NanoHTTPD(8080) {
    interface WebPortListener {
        fun onCredentialsSubmit(ssid: String, password: String)
    }

    override fun serve(session: IHTTPSession?): Response? {
        return when(session?.method) {
            Method.GET -> {
                val html = """
                    <html>
                        <body>
                            <form method="POST">
                                SSID: <input type="text" name="ssid"><br>
                                Password: <input type="password" name="password"><br>
                                <input type="submit" value="Submit">
                            </form>
                        </body>
                    </html>
                """.trimIndent()
                newFixedLengthResponse(Response.Status.OK, "text/html", html)
            }
            Method.POST -> {
                val params = mutableMapOf<String, String>()
                session.parseBody(params)
                val ssid = params["ssid"] ?: ""
                val password = params["password"] ?: ""
                listener.onCredentialsSubmit(ssid, password)
                newFixedLengthResponse(Response.Status.OK, "text/html", "Credentials received")
            }
            else -> newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed")
        }
    }
}