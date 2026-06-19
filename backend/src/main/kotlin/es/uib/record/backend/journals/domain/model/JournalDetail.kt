package es.uib.record.backend.journals.domain.model

/** Detalle completo de una revista: sus métricas por año y sus cuartiles por categoría y año. */
data class JournalDetail(
    val journal: Journal,
    val metrics: List<JournalMetric>,
    val categoryQuartiles: List<JournalCategoryQuartileInfo>,
)
