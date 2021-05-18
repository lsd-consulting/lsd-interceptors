package com.nickmcdowall.lsd.repository.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.PrettyPrinter;
import com.nickmcdowall.lsd.http.naming.AppName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.ZonedDateTime;

import static com.nickmcdowall.lsd.http.common.PrettyPrinter.prettyPrint;
import static j2html.TagCreator.*;
import static java.lang.System.lineSeparator;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Slf4j
@RequiredArgsConstructor
public class AopInterceptorDelegate {
    private final TestState testState;
    private final AppName appName;

    public void captureInternalInteraction(JoinPoint joinPoint, Object resultValue, String icon) {
        captureInteraction(joinPoint, resultValue, appName.getValue(), appName.getValue(), icon);
    }

    public void captureInteraction(JoinPoint joinPoint, Object resultValue, String sourceName, String destinationName, String icon) {
        var args = joinPoint.getArgs();
        var body = renderHtmlForMethodCall(args, resultValue);
        var signature = joinPoint.getSignature().toShortString();

        testState.log(icon + " " + signature + " from " + sourceName + " to " + destinationName, body);
    }

    public void captureInternalException(JoinPoint joinPoint, Throwable throwable, String icon) {
        captureException(joinPoint, throwable, appName.getValue(), appName.getValue(), icon);
    }

    public void captureException(JoinPoint joinPoint, Throwable throwable, String sourceName, String destinationName, String icon) {
        var body = renderHtmlForException(joinPoint.getSignature().toShortString(), joinPoint.getArgs(), throwable);
        var exceptionName = throwable.getClass().getSimpleName();
        testState.log(icon + " " + exceptionName + " response from " + sourceName + " to " + destinationName + " [#red]", body);
    }

    public void captureScheduledStart(ProceedingJoinPoint joinPoint, ZonedDateTime startTime) {
        testState.log("<$clock{scale=0.4,color=skyblue}> scheduler <$play{scale=0.4,color=skyblue}> from ? to " + appName.getValue(),
                p(
                        p(
                                h4("Triggered"),
                                sub(joinPoint.getSignature().toShortString())
                        ),
                        p(
                                h4("Timestamp"),
                                sub(startTime.format(ISO_DATE_TIME))
                        )
                ).render());
    }

    public void captureScheduledEnd(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime) {
        testState.log("<$clock{scale=0.4,color=skyblue}> scheduler <$stop{scale=0.4,color=skyblue}> from ? to " + appName.getValue(),
                p(
                        p(
                                h4("Triggered"),
                                sub(joinPoint.getSignature().toShortString())
                        ),
                        p(
                                h4("Duration"),
                                sub(MILLIS.between(startTime, endTime) + "ms")
                        )
                ).render());
    }

    public void captureScheduledError(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime, Throwable e) {
        testState.log("<$clock{scale=0.4,color=red}> scheduler <$exclamation{scale=0.4,color=red}> from ? to " + appName.getValue() + " [#red]",
                p(
                        p(
                                h4("Triggered"),
                                sub(joinPoint.getSignature().toShortString())
                        ),
                        p(
                                h4("Exception"),
                                sub(e.toString())
                        ),
                        p(
                                h4("Duration"),
                                sub(MILLIS.between(startTime, endTime) + "ms")
                        )
                ).render());
    }

    private String renderHtmlForMethodCall(Object[] args, Object response) {
        return p(
                p(
                        h4("Arguments:"),
                        sub(prettyPrintArgs(args))
                ),
                p(
                        h4("Response:"),
                        pre(ofNullable(response)
                                .map(r -> prettyPrint(r.toString()))
                                .orElse("")
                        )
                )
        ).render();
    }

    private String renderHtmlForException(String signature, Object[] args, Throwable throwable) {
        return p(
                p(
                        h4("Invoked:"),
                        sub(signature)
                ),
                p(
                        h4("Arguments:"),
                        sub(prettyPrintArgs(args))
                ),
                p(
                        h4("Exception:"),
                        pre(throwable.toString())
                )
        ).render();
    }

    private String prettyPrintArgs(Object[] args) {
        return stream(args)
                .map(Object::toString)
                .map(PrettyPrinter::prettyPrint)
                .collect(joining(lineSeparator()));
    }
}
