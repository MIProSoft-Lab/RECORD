package es.uib.record.backend.journals.infrastructure.client

import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateBibliographicResponse
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateJournalsPageResponse
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateMetricsResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient

/**
 * Cliente HTTP contra la API de Clarivate (Web of Science Journals v1). Reimplementa los scripts
 * Python de referencia. Respeta el límite de la API (peticiones por segundo) mediante un throttle y
 * reintenta con backoff ante 429/5xx.
 */
@Component
class ClarivateClient(
    @Qualifier("clarivateRestClient") private val restClient: RestClient,
    @Value($$"${application.clarivate.sync.rate-limit-per-second:4}") rateLimitPerSecond: Int,
) {
    private val logger = LoggerFactory.getLogger(ClarivateClient::class.java)

    private val minIntervalMillis: Long = 1000L / rateLimitPerSecond.coerceAtLeast(1)
    private var lastRequestAt: Long = 0L

    @Synchronized
    private fun throttle() {
        val now = System.currentTimeMillis()
        val wait = minIntervalMillis - (now - lastRequestAt)
        if (wait > 0) Thread.sleep(wait)
        lastRequestAt = System.currentTimeMillis()
    }

    /** Devuelve el cuerpo crudo de `/last-updated` para comparar contra el valor almacenado. */
    fun getLastUpdated(): String? =
        withRetry("last-updated") {
            restClient.get().uri("/last-updated").retrieve().body(String::class.java)
        }

    fun getJournalsPage(page: Int, limit: Int): ClarivateJournalsPageResponse =
        withRetry("journals page $page") {
            restClient
                .get()
                .uri {
                    it.path("/journals").queryParam("limit", limit).queryParam("page", page).build()
                }
                .retrieve()
                .body(ClarivateJournalsPageResponse::class.java)
        } ?: ClarivateJournalsPageResponse()

    fun getBibliographic(clarivateId: String): ClarivateBibliographicResponse? =
        withRetryOrNull("bibliographic $clarivateId") {
            restClient
                .get()
                .uri("/journals/{id}", clarivateId)
                .retrieve()
                .body(ClarivateBibliographicResponse::class.java)
        }

    fun getMetrics(clarivateId: String, year: Int): ClarivateMetricsResponse? =
        withRetryOrNull("metrics $clarivateId/$year") {
            restClient
                .get()
                .uri("/journals/{id}/reports/year/{year}", clarivateId, year)
                .retrieve()
                .body(ClarivateMetricsResponse::class.java)
        }

    /** Reintenta ante 429/5xx; los 4xx (salvo 429) propagan. */
    private fun <T> withRetry(description: String, block: () -> T): T {
        var attempt = 0
        while (true) {
            throttle()
            try {
                return block()
            } catch (e: HttpServerErrorException) {
                attempt = retryOrThrow(description, attempt, e)
            } catch (e: HttpClientErrorException.TooManyRequests) {
                attempt = retryOrThrow(description, attempt, e)
            }
        }
    }

    /** Igual que [withRetry], pero devuelve null ante un 404 (recurso inexistente). */
    private fun <T> withRetryOrNull(description: String, block: () -> T): T? =
        try {
            withRetry(description, block)
        } catch (e: HttpClientErrorException.NotFound) {
            logger.debug("Clarivate resource not found ({}): {}", description, e.message)
            null
        }

    private fun retryOrThrow(description: String, attempt: Int, e: RuntimeException): Int {
        if (attempt >= MAX_RETRIES) {
            logger.error("Clarivate request failed after {} retries ({})", MAX_RETRIES, description)
            throw e
        }
        val backoff = BASE_BACKOFF_MILLIS * (1L shl attempt)
        logger.warn(
            "Clarivate request {} failed ({}); retrying in {} ms",
            description,
            e.message,
            backoff,
        )
        Thread.sleep(backoff)
        return attempt + 1
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_BACKOFF_MILLIS = 1000L
    }
}
