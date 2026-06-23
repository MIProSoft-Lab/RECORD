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

    /** Indica si existe una revista con el [journalId] dado. */
    fun existsById(journalId: UUID): Boolean

    /** Devuelve, para los [journalIds] existentes, su referencia mínima indexada por id. */
    fun getJournalsByIds(journalIds: Set<UUID>): Map<UUID, JournalRefDto>
}
