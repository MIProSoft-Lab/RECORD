package es.uib.record.backend.journals.infrastructure.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClarivateMetricsResponse(
    val year: Int,
    val suppressed: Boolean = false,
    val metrics: Metrics? = null,
    val ranks: Ranks? = null,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Metrics(val impactMetrics: ImpactMetrics? = null)

    @JsonIgnoreProperties(ignoreUnknown = true) data class ImpactMetrics(val jif: String? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Ranks(val jif: List<JifRank> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class JifRank(
        val category: String? = null,
        val edition: String? = null,
        val quartile: String? = null,
    )
}
