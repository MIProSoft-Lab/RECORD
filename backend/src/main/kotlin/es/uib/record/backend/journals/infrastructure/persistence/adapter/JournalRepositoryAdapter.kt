package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.infrastructure.mapper.toDomain
import es.uib.record.backend.journals.infrastructure.mapper.toEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class JournalRepositoryAdapter(
    private val springDataJpaJournalRepository: SpringDataJpaJournalRepository
) : JournalRepository {

    override fun save(journal: Journal): Journal {
        return this.springDataJpaJournalRepository.save(journal.toEntity()).toDomain()
    }

    override fun findByClarivateId(clarivateId: String): Journal? {
        return this.springDataJpaJournalRepository.findByClarivateId(clarivateId)?.toDomain()
    }

    override fun markSynced(id: UUID, syncedAt: Instant) {
        this.springDataJpaJournalRepository.markSynced(id, syncedAt)
    }
}
