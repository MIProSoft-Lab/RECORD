package es.uib.record.backend.journals.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class JournalMetric(
    val id: UUID? = null,
    val journalId: UUID,
    val year: Int,
    val impactFactor: BigDecimal? = null,
    val createdAt: Instant = Instant.now(),
)
