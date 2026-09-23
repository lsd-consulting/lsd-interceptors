package io.lsdconsulting.interceptors.http.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.http.LsdRestTemplateCustomizer
import io.lsdconsulting.interceptors.http.LsdRestTemplateInterceptor
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.restclient.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate

/**
 *
 *
 * If a [RestTemplate] class and a [LsdContext] class is available it will automatically autoconfig
 * a [LsdRestTemplateInterceptor]
 *
 * <br></br>
 *
 *
 * It is assumed that if a [RestTemplate] bean exists is will be used to invoke downstream endpoints from within the app.
 * Therefore the *source* name will default to *'App'* and the *destination* name will be derived using a
 * [io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper] by default.
 *
 * <br></br>
 *
 *
 * Users can override either or both of the default name mappings by supplying their own [io.lsdconsulting.interceptors.http.naming.SourceNameMappings] or
 * [io.lsdconsulting.interceptors.http.naming.DestinationNameMappings] beans and naming them *'defaultSourceNameMapping`* and *'defaultDestinationNameMapping`*.
 *
 */
@Configuration
@ConditionalOnProperty(name = ["lsd.interceptors.autoconfig.enabled"], havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = [RestTemplate::class, LsdContext::class])
@Import(NamingConfig::class, HttpHandlerConfig::class)
open class LsdRestTemplateAutoConfiguration(
    private val httpInteractionHandlers: List<HttpInteractionHandler>
) {

    @Bean
    open fun restTemplateCustomizer(): RestTemplateCustomizer {
        return LsdRestTemplateCustomizer(LsdRestTemplateInterceptor(httpInteractionHandlers))
    }
}
