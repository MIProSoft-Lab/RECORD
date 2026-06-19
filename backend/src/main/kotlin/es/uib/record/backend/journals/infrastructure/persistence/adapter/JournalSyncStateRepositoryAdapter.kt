package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import es.uib.record.backend.journals.infrastructure.mapper.toDomain
import es.uib.record.backend.journals.infrastructure.mapper.toEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalSyncStateRepository
import java.lang.IllegalStateException
import org.springframework.stereotype.Repository

@Repository
class JournalSyncStateRepositoryAdapter(
    private val springDataJpaJournalSyncStateRepository: SpringDataJpaJournalSyncStateRepository
) : JournalSyncStateRepository {

    override fun get(): SyncState {
        return this.springDataJpaJournalSyncStateRepository.findAll().firstOrNull()?.toDomain()
            ?: throw IllegalStateException(
                "journal_sync_state row is missing; it must be seeded by Flyway"
            )
    }

    override fun save(syncState: SyncState): SyncState {
        return this.springDataJpaJournalSyncStateRepository.save(syncState.toEntity()).toDomain()
    }
}
