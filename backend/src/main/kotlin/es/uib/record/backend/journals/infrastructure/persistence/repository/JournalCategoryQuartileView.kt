package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.domain.model.Quartile
import java.math.BigDecimal
import java.util.UUID

/**
 * Proyección de una fila de cuartil junto al nombre/edición de su categoría, poblada vía expresión
 * `new` en JPQL para evitar el join manual de tuplas en la capa de dominio.
 */
data class JournalCategoryQuartileView(
    val journalId: UUID,
    val categoryId: UUID,
    val categoryName: String,
    val edition: String?,
    val year: Int,
    val quartile: Quartile,
    val impactFactor: BigDecimal?,
)
