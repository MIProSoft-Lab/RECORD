package es.uib.record.backend.journals.domain.model

/**
 * Resultado de búsqueda: una revista con sus categorías/cuartiles de su último año disponible.
 * [year] es ese último año (nulo si la revista no tiene datos de cuartiles). [isInterest] indica si
 * el usuario autenticado ha marcado la revista como de interés.
 */
data class JournalSearchItem(
    val journal: Journal,
    val year: Int?,
    val categories: List<JournalCategoryQuartileInfo>,
    val isInterest: Boolean = false,
)
