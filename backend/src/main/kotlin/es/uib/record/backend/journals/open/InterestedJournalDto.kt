package es.uib.record.backend.journals.open

import java.util.UUID

/**
 * Revista marcada como de interés por un conjunto de usuarios, expuesta a otros módulos.
 * [interestedUserIds] son los usuarios (del conjunto consultado) que la han marcado.
 */
data class InterestedJournalDto(
    val journalId: UUID,
    val name: String,
    val issn: String?,
    val eIssn: String?,
    val publisherName: String?,
    val year: Int?,
    val categories: List<InterestedJournalCategoryDto>,
    val interestedUserIds: List<UUID>,
)

data class InterestedJournalCategoryDto(
    val categoryId: UUID,
    val categoryName: String,
    val edition: String?,
    val quartile: String,
    val impactFactor: Double?,
)
