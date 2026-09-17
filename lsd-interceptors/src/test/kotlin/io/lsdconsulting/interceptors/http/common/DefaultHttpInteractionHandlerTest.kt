package io.lsdconsulting.interceptors.http.common

import com.lsd.core.LsdContext
import com.lsd.core.domain.Message
import com.lsd.core.domain.MessageType
import com.lsd.core.domain.ParticipantType
import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.interceptors.common.HeaderKeys
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class DefaultHttpInteractionHandlerTest {
    private val serviceNameHeaders = mapOf(
        HeaderKeys.TARGET_NAME.key() to "bob",
        HeaderKeys.SOURCE_NAME.key() to "juliet",
    )
    private val sourceNameMapping = SourceNameMappings { "andrea" }
    private val destinationNameMapping = DestinationNameMappings { "bren" }
    private val capturedEvents = mutableListOf<SequenceEvent>()
    private val lsdContext = object : LsdContext() {
        override fun capture(vararg events: SequenceEvent) {
            capturedEvents += events
            super.capture(*events)
        }
    }
    private val handler = DefaultHttpInteractionHandler(lsdContext, sourceNameMapping, destinationNameMapping)
    private val bob = ParticipantType.PARTICIPANT.called("bob")
    private val juliet = ParticipantType.PARTICIPANT.called("juliet")
    private val bren = ParticipantType.PARTICIPANT.called("bren")
    private val andrea = ParticipantType.PARTICIPANT.called("andrea")

    @Test
    fun usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}")
        val (_, from, to, label, type, _, data) = extractFirstMessage()
        assertThat(from).isEqualTo(andrea)
        assertThat(to).isEqualTo(bren)
        assertThat(label).isEqualTo("GET /path")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS)
        assertThat(data.toString()).contains("<p>{\n  &quot;type&quot;: &quot;request&quot;\n}</p>")
    }

    @Test
    fun usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), emptyMap(), "/path", "response body", Duration.ofMillis(5))
        val (_, from, to, label, type, _, data, duration) = extractFirstMessage()
        assertThat(from).isEqualTo(bren)
        assertThat(to).isEqualTo(andrea)
        assertThat(label).isEqualTo("200 OK (5ms)")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE)
        assertThat(data.toString()).contains("<p>response body</p")
        assertThat(duration).isEqualTo(Duration.ofMillis(5))
    }

    @Test
    fun headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "")
        val (_, from, to, label, type, _, data) = extractFirstMessage()
        assertThat(from).isEqualTo(juliet)
        assertThat(to).isEqualTo(bob)
        assertThat(label).isEqualTo("GET /path")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS)
        assertThat(data.toString()).contains("Source-Name: juliet").contains("Target-Name: bob")
    }

    @Test
    fun headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, emptyMap(), "/path", "response body", Duration.ofMillis(3))
        val (_, from, to, label, type, _, data, duration) = extractFirstMessage()
        assertThat(from).isEqualTo(bob)
        assertThat(to).isEqualTo(juliet)
        assertThat(label).isEqualTo("200 OK (3ms)")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE)
        assertThat(duration).isEqualTo(Duration.ofMillis(3))
        assertThat(data.toString())
            .contains("<h3>Request Headers</h3>")
            .contains("Target-Name: bob")
            .contains("Source-Name: juliet")
            .contains("<p>response body</p>")
    }

    private fun extractFirstMessage(): Message =
        capturedEvents.filterIsInstance<Message>().firstOrNull()
            ?: error("No Message captured: $capturedEvents")
}
