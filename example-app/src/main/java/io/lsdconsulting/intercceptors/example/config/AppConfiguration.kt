package io.lsdconsulting.intercceptors.example.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
class AppConfiguration {
    @Bean
    fun converter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter(ObjectMapper())
    }
}
