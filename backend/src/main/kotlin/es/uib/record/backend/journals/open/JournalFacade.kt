package es.uib.record.backend.journals.open

import es.uib.record.backend.shared.domain.PageResult
import java.util.UUID

/** Acceso de otros módulos a la información de revistas de interés. */
interface JournalFacade {
    /**
     * Unión paginada de las revistas marcadas como de interés por cualquiera de los [userIds],
     * deduplicada y ordenada por número de usuarios que la marcan (descendente).
     */
    fun getJournalsInterestedByUsers(
        userIds: Set<UUID>,
        page: Int,
        size: Int,
    ): PageResult<InterestedJournalDto>
}
