package com.sdk.adhocsdk.Http

import com.koushikdutta.async.http.server.AsyncHttpServer


class HttpServer {
    fun start() {
        val server = AsyncHttpServer()
        server.get(
            "/"
        ) { request, response -> response.send("Hello!!!") }

        server.listen(5000)
    }
}