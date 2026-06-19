package es.uib.record.backend.journals.domain.model

import java.math.BigDecimal
import java.util.UUID

/** Posición de una revista en una categoría para un año, resuelta con el nombre de la categoría. */
data class JournalCategoryQuartileInfo(
    val categoryId: UUID,
    val categoryName: String,
    val edition: String?,
    val year: Int,
    val quartile: Quartile,
    val impactFactor: BigDecimal?,
)
