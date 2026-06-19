package es.uib.record.backend.journals.infrastructure.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClarivateBibliographicResponse(
    val id: String? = null,
    val name: String? = null,
    val issn: String? = null,
    val eIssn: String? = null,
    val publisher: Publisher? = null,
    val journalCitationReports: List<JournalCitationReport> = emptyList(),
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Publisher(val name: String? = null, val countryRegion: String? = null)

    @JsonIgnoreProperties(ignoreUnknown = true) data class JournalCitationReport(val year: Int)
}
