package es.uib.record.backend.journals.domain.model

import java.util.UUID

/**
 * Revista marcada como de interés por un conjunto de usuarios, con sus categorías/cuartiles del
 * último año disponible. [interestedUserIds] son los usuarios (del conjunto consultado) que la han
 * marcado; su tamaño es el número de marcas. [year] es el último año con datos de cuartiles (nulo
 * si no hay).
 */
data class InterestedJournal(
    val journal: Journal,
    val year: Int?,
    val categories: List<JournalCategoryQuartileInfo>,
    val interestedUserIds: List<UUID>,
)
