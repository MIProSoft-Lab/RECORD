package es.uib.record.backend.journals.domain.repository

import es.uib.record.backend.journals.domain.model.Journal
import java.time.Instant
import java.util.UUID

interface JournalRepository {
    fun save(journal: Journal): Journal

    fun findByClarivateId(clarivateId: String): Journal?

    fun markSynced(id: UUID, syncedAt: Instant)
}
