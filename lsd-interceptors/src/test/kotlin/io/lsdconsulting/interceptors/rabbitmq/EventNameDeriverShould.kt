package io.lsdconsulting.interceptors.rabbitmq

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.MessageProperties

internal class EventNameDeriverShould {

    @Test
    fun `return type id header without packages`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "SomeClassName")

        val result = deriveEventName(messageProperties, "alternative")

        assertThat(result).isEqualTo("SomeClassName")
    }

    @Test
    fun `return type id header with packages`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.SomeClassName")

        val result = deriveEventName(messageProperties, "alternative")

        assertThat(result).isEqualTo("SomeClassName")
    }

    @Test
    fun `return alternative value`() {
        val messageProperties = MessageProperties()

        val result = deriveEventName(messageProperties, "alternative")

        assertThat(result).isEqualTo("alternative")
    }

    @Test
    fun `return unknown event`() {
        val messageProperties = MessageProperties()

        val result = deriveEventName(messageProperties, "")

        assertThat(result).isEqualTo("UNKNOWN_EVENT")
    }

    @Test
    fun `handle null alternative exchange name`() {
        val messageProperties = MessageProperties()

        val result = deriveEventName(messageProperties, null)

        assertThat(result).isEqualTo("UNKNOWN_EVENT")
    }

    @Test
    fun `handle empty type id header`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "")

        val result = deriveEventName(messageProperties, "alternative")

        assertThat(result).isEqualTo("alternative")
    }

    @Test
    fun `handle spaces only in type id header`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "  ")

        val result = deriveEventName(messageProperties, "alternative")

        assertThat(result).isEqualTo("alternative")
    }

    @Test
    fun `handle null type id header`() {
        val messageProperties = MessageProperties()
        @Suppress("UNCHECKED_CAST")
        (messageProperties.headers as MutableMap<String, Any?>)["__TypeId__"] = null

        val result = deriveEventName(messageProperties, "alternative")

        assertThat(result).isEqualTo("alternative")
    }
}
