package io.lsdconsulting.interceptors.aop

import com.lsd.core.LsdContext
import com.lsd.core.builders.LifelineBuilder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.AppName
import j2html.TagCreator
import lsd.format.prettyPrint
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors

class AopInterceptorDelegate(
    private val lsdContext: LsdContext,
    private val appName: AppName,

) {

    fun captureInternalInteraction(joinPoint: JoinPoint, resultValue: Any?, icon: String) {
        captureInteraction(joinPoint, resultValue, appName.value, appName.value, icon)
    }

    fun captureInteraction(
        joinPoint: JoinPoint,
        resultValue: Any?,
        sourceName: String,
        destinationName: String,
        icon: String
    ) {
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .from(sourceName)
                .to(destinationName)
                .label(icon + " " + joinPoint.signature.toShortString())
                .data(renderHtmlForMethodCall(joinPoint.args, resultValue))
                .build()
        )
    }

    fun captureInternalException(joinPoint: JoinPoint, throwable: Throwable, icon: String) {
        captureException(joinPoint, throwable, appName.value, appName.value, icon)
    }

    fun captureException(
        joinPoint: JoinPoint,
        throwable: Throwable,
        sourceName: String,
        destinationName: String,
        icon: String
    ) {
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .from(sourceName)
                .to(destinationName)
                .label(icon + " " + throwable.javaClass.simpleName)
                .type(MessageType.SYNCHRONOUS_RESPONSE)
                .colour("red")
                .data(renderHtmlForException(joinPoint.signature.toShortString(), joinPoint.args, throwable))
                .build()
        )
    }

    fun captureScheduledStart(joinPoint: ProceedingJoinPoint, startTime: ZonedDateTime) {
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .to(appName.value)
                .label("<\$clock{scale=0.3}> ")
                .type(MessageType.SHORT_INBOUND)
                .data(
                    TagCreator.div(
                        TagCreator.section(
                            TagCreator.h3("Scheduled"),
                            TagCreator.span(joinPoint.signature.toShortString())
                        ),
                        TagCreator.section(
                            TagCreator.h3("Timestamp"),
                            TagCreator.span(startTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        )
                    ).render()
                )
                .build(),
            LifelineBuilder.activation().of(appName.value).colour("skyblue").build()
        )
    }

    fun captureScheduledEnd(joinPoint: ProceedingJoinPoint, startTime: ZonedDateTime?, endTime: ZonedDateTime?) {
        val duration = ChronoUnit.MILLIS.between(startTime, endTime)
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .to(appName.value)
                .label("<\$clock{scale=0.3}>")
                .data(
                    TagCreator.div(
                        TagCreator.section(
                            TagCreator.h3("Scheduler completed"),
                            TagCreator.span(joinPoint.signature.toShortString())
                        ),
                        TagCreator.section(
                            TagCreator.h3("Duration"),
                            TagCreator.span(duration.toString() + "ms")
                        )
                    ).render()
                )
                .type(MessageType.SHORT_INBOUND)
                .duration(Duration.ofMillis(duration))
                .build(),
            LifelineBuilder.deactivation().of(appName.value).build()
        )
    }

    fun captureScheduledError(
        joinPoint: ProceedingJoinPoint,
        startTime: ZonedDateTime?,
        endTime: ZonedDateTime?,
        e: Throwable
    ) {
        val duration = ChronoUnit.MILLIS.between(startTime, endTime)
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .to(appName.value)
                .label("<\$clock{scale=0.3}> ")
                .type(MessageType.SHORT_INBOUND)
                .colour("red")
                .data(
                    TagCreator.div(
                        TagCreator.section(
                            TagCreator.h3("Scheduler Error"),
                            TagCreator.span(joinPoint.signature.toShortString())
                        ),
                        TagCreator.section(
                            TagCreator.h3("Exception"),
                            TagCreator.span(e.toString())
                        ),
                        TagCreator.section(
                            TagCreator.h3("Duration"),
                            TagCreator.span(duration.toString() + "ms")
                        )
                    ).render()
                )
                .duration(Duration.ofMillis(duration))
                .build()
        )
    }

    private fun renderHtmlForMethodCall(args: Array<Any>, response: Any?): String {
        return TagCreator.div(
            TagCreator.section(
                TagCreator.h3("Arguments"),
                TagCreator.span(prettyPrintArgs(args))
            ),
            TagCreator.section(
                TagCreator.h3("Response"),
                TagCreator.p(
                    Optional.ofNullable<Any>(response)
                        .map { obj: Any? -> prettyPrint(obj) }
                        .orElse("")
                )
            )
        ).render()
    }

    private fun renderHtmlForException(signature: String, args: Array<Any>, throwable: Throwable): String {
        return TagCreator.div(
            TagCreator.section(TagCreator.h3("Invoked"), TagCreator.span(signature)),
            TagCreator.section(TagCreator.h3("Arguments"), TagCreator.span(prettyPrintArgs(args))),
            TagCreator.section(TagCreator.h3("Exception"), TagCreator.p(throwable.toString()))
        ).render()
    }

    private fun prettyPrintArgs(args: Array<Any>): String {
        return Arrays.stream(args)
            .map { obj: Any? -> prettyPrint(obj) }
            .collect(Collectors.joining(System.lineSeparator()))
    }
}
