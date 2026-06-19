package es.uib.record.backend.journals.domain.model

/**
 * Resultado de búsqueda: una revista con sus categorías/cuartiles de su último año disponible.
 * [year] es ese último año (nulo si la revista no tiene datos de cuartiles).
 */
data class JournalSearchItem(
    val journal: Journal,
    val year: Int?,
    val categories: List<JournalCategoryQuartileInfo>,
)
