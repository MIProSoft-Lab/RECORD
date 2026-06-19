package es.uib.record.backend.journals.domain.model

import java.math.BigDecimal
import java.util.UUID

/**
 * Cuartil de un journal en una categoría para un año concreto. Es una entidad de primera clase (no
 * una colección embebida) y desnormaliza `journalId`, `year` e `impactFactor` para que las futuras
 * búsquedas por categoría + cuartil + año se resuelvan sobre una sola tabla indexada.
 */
data class JournalCategoryQuartile(
    val id: UUID? = null,
    val journalMetricId: UUID,
    val journalId: UUID,
    val categoryId: UUID,
    val year: Int,
    val quartile: Quartile,
    val impactFactor: BigDecimal? = null,
)
