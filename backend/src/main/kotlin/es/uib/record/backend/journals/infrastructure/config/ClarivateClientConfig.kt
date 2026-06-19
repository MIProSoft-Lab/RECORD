package es.uib.record.backend.journals.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class ClarivateClientConfig {

    @Bean("clarivateRestClient")
    fun clarivateRestClient(
        @Value($$"${application.clarivate.base-url}") baseUrl: String,
        @Value($$"${application.clarivate.api-key}") apiKey: String,
    ): RestClient {
        return RestClient.builder().baseUrl(baseUrl).defaultHeader("X-ApiKey", apiKey).build()
    }
}
