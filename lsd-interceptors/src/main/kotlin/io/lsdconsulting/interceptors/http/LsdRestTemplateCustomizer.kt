package io.lsdconsulting.interceptors.http

import org.springframework.boot.restclient.RestTemplateCustomizer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

data class LsdRestTemplateCustomizer(
    private var interceptor: ClientHttpRequestInterceptor
) : RestTemplateCustomizer {

    override fun customize(restTemplate: RestTemplate) {
        val interceptors = restTemplate.interceptors
        if (!interceptors.contains(interceptor)) {
            interceptors.add(interceptor)
        }
        restTemplate.requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
    }
}
