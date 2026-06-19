package es.uib.record.backend.journals.domain.repository

import java.util.UUID

/** Relación por-usuario de revistas marcadas como de interés. */
interface UserJournalInterestRepository {
    /** Marca la revista como de interés para el usuario. Idempotente. */
    fun add(userId: UUID, journalId: UUID)

    /** Quita la revista de los intereses del usuario. Idempotente. */
    fun remove(userId: UUID, journalId: UUID)

    /** Indica si el usuario tiene la revista marcada como de interés. */
    fun exists(userId: UUID, journalId: UUID): Boolean

    /** IDs de las revistas que el usuario ha marcado como de interés. */
    fun findInterestJournalIds(userId: UUID): Set<UUID>
}
