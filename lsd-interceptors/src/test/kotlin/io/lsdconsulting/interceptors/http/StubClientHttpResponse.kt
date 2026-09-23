package io.lsdconsulting.interceptors.http

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import java.io.InputStream

data class StubClientHttpResponse(
    private val body: InputStream,
    private val headers: HttpHeaders,
    private val statusCode: HttpStatus
) : ClientHttpResponse {
    override fun getStatusCode(): HttpStatus {
        return statusCode
    }


    override fun getStatusText(): String {
        return statusCode.value().toString() + statusCode.reasonPhrase
    }

    override fun close() {
        body.close()
    }

    override fun getBody(): InputStream {
        return body
    }

    override fun getHeaders(): HttpHeaders {
        return headers
    }
}
