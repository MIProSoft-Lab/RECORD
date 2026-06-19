package es.uib.record.backend.journals.domain.model

import java.time.Instant
import java.util.UUID

data class Journal(
    val id: UUID? = null,
    val clarivateId: String,
    val name: String,
    val issn: String? = null,
    val eIssn: String? = null,
    val publisherName: String? = null,
    val publisherCountry: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant? = null,
    val lastSyncedAt: Instant? = null,
)
