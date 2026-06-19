package es.uib.record.backend.journals.infrastructure.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClarivateJournalsPageResponse(
    val metadata: Metadata = Metadata(),
    val hits: List<Hit> = emptyList(),
) {
    @JsonIgnoreProperties(ignoreUnknown = true) data class Metadata(val total: Int = 0)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Hit(val id: String, val name: String? = null)
}
