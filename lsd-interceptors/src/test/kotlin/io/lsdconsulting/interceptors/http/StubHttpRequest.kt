package io.lsdconsulting.interceptors.http

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import java.net.URI

data class StubHttpRequest(
    private val methodValue: String,
    private val uri: URI,
    private val httpHeaders: HttpHeaders
) : HttpRequest {
    override fun getHeaders(): HttpHeaders = httpHeaders
    override fun getMethod(): HttpMethod = HttpMethod.valueOf(methodValue)
    override fun getURI(): URI = uri
    override fun getAttributes(): MutableMap<String, Any> = mutableMapOf()
}
