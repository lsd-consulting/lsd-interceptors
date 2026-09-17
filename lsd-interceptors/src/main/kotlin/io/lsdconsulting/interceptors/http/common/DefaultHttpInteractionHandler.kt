package io.lsdconsulting.interceptors.http.common

import com.lsd.core.IdGenerator
import com.lsd.core.LsdContext
import com.lsd.core.builders.LifelineBuilder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys
import io.lsdconsulting.interceptors.common.HeaderKeys.TARGET_NAME
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings
import j2html.TagCreator
import lsd.format.prettyPrint
import org.apache.commons.lang3.StringUtils
import java.time.Duration
import java.util.stream.Collectors

data class DefaultHttpInteractionHandler(
    private val lsdContext: LsdContext,
    private val sourceNameMappings: SourceNameMappings,
    private val destinationNameMappings: DestinationNameMappings
) : HttpInteractionHandler {
    private val idGenerator: IdGenerator = lsdContext.idGenerator

    override fun handleRequest(method: String, requestHeaders: Map<String, String>, path: String, body: String) {
        val targetName = deriveTargetName(requestHeaders, path)
        lsdContext.capture(
            messageBuilder()
                .id(idGenerator.next())
                .from(deriveSourceName(requestHeaders, path))
                .to(targetName)
                .label("$method $path")
                .data(renderHtmlFor(path, requestHeaders, null, prettyPrint(body), null))
                .type(MessageType.SYNCHRONOUS)
                .build(),
            LifelineBuilder.activation().of(targetName).colour("skyblue").build()
        )
    }

    override fun handleResponse(
        statusMessage: String,
        requestHeaders: Map<String, String>,
        responseHeaders: Map<String, String>,
        path: String,
        body: String,
        duration: Duration
    ) {
        var colour = ""
        if (statusMessage.startsWith("4") || statusMessage.startsWith("5")) colour = "red"
        val targetName = deriveTargetName(requestHeaders, path)
        lsdContext.capture(
            messageBuilder()
                .id(idGenerator.next())
                .from(targetName)
                .to(deriveSourceName(requestHeaders, path))
                .label(statusMessage + " (" + duration.toMillis() + "ms)")
                .data(renderHtmlFor(path, requestHeaders, responseHeaders, prettyPrint(body), duration))
                .type(MessageType.SYNCHRONOUS_RESPONSE)
                .colour(colour)
                .duration(duration)
                .build(),
            LifelineBuilder.deactivation().of(targetName).build()
        )
    }

    private fun renderHtmlFor(
        path: String,
        requestHeaders: Map<String, String>,
        responseHeaders: Map<String, String>?,
        prettyBody: String,
        duration: Duration?
    ): String {
        return TagCreator.div(
            TagCreator.section(
                TagCreator.h3("Full Path"),
                TagCreator.span(path)
            ),
            if (isMissingHeaders(requestHeaders)) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Request Headers"),
                TagCreator.p(prettyPrintHeaders(requestHeaders))
            ),
            if (isMissingHeaders(responseHeaders)) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Response Headers"),
                TagCreator.p(prettyPrintHeaders(responseHeaders))
            ),
            if (StringUtils.isEmpty(prettyBody)) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Body"),
                TagCreator.p(prettyBody),
                if (null == duration) TagCreator.p() else TagCreator.section(
                    TagCreator.h3("Duration"),
                    TagCreator.p(duration.toMillis().toString() + "ms")
                )
            )
        ).render()
    }

    private fun prettyPrintHeaders(headers: Map<String, String>?): String {
        return headers!!.entries.stream().map { (key, value): Map.Entry<String, String> -> "$key: $value" }
            .collect(Collectors.joining(System.lineSeparator()))
    }

    private fun deriveTargetName(headers: Map<String, String>, path: String): String {
        return if (headers.containsKey(TARGET_NAME.key())) headers[TARGET_NAME.key()]!! else destinationNameMappings.mapForPath(
            path
        )
    }

    private fun deriveSourceName(headers: Map<String, String>, path: String): String {
        return if (headers.containsKey(HeaderKeys.SOURCE_NAME.key())) headers[HeaderKeys.SOURCE_NAME.key()]!! else sourceNameMappings.mapForPath(
            path
        )
    }

    companion object {
        private fun isMissingHeaders(requestHeaders: Map<String, String>?): Boolean {
            return requestHeaders.isNullOrEmpty()
        }
    }
}
