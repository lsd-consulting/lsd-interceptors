package io.lsdconsulting.interceptors.messaging;

import com.lsd.core.LsdContext;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import static com.lsd.core.builders.MessageBuilder.messageBuilder;
import static com.lsd.core.domain.MessageType.ASYNCHRONOUS;
import static io.lsdconsulting.interceptors.common.Headers.HeaderKeys.SOURCE_NAME;
import static io.lsdconsulting.interceptors.common.Headers.HeaderKeys.TARGET_NAME;
import static io.lsdconsulting.interceptors.messaging.HtmlRenderer.renderHtmlFor;
import static io.lsdconsulting.interceptors.messaging.TypeConverter.convertToString;
import static lsd.format.PrettyPrinter.prettyPrint;

@RequiredArgsConstructor
public class EventPublisherInterceptor implements ChannelInterceptor {

    private final LsdContext lsdContext;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        String payload = prettyPrint(convertToString(message.getPayload()));
        String source = convertToString(message.getHeaders().get(SOURCE_NAME.key()));
        String target = convertToString(message.getHeaders().get(TARGET_NAME.key()));

        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .from(source)
                .to(target)
                .label("Publish event")
                .data(renderHtmlFor(message.getHeaders(), payload))
                .type(ASYNCHRONOUS)
                .build());

        return message;
    }
}