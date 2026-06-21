package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.journals.infrastructure.persistence.entity.UserJournalInterestEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaUserJournalInterestRepository
import java.util.UUID
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserJournalInterestRepositoryAdapter(
    private val springDataJpaUserJournalInterestRepository:
        SpringDataJpaUserJournalInterestRepository
) : UserJournalInterestRepository {

    @Transactional
    override fun add(userId: UUID, journalId: UUID) {
        if (
            !this.springDataJpaUserJournalInterestRepository.existsByUserIdAndJournalId(
                userId,
                journalId,
            )
        ) {
            this.springDataJpaUserJournalInterestRepository.save(
                UserJournalInterestEntity(userId = userId, journalId = journalId)
            )
        }
    }

    @Transactional
    override fun remove(userId: UUID, journalId: UUID) {
        this.springDataJpaUserJournalInterestRepository.deleteByUserIdAndJournalId(
            userId,
            journalId,
        )
    }

    override fun exists(userId: UUID, journalId: UUID): Boolean {
        return this.springDataJpaUserJournalInterestRepository.existsByUserIdAndJournalId(
            userId,
            journalId,
        )
    }

    override fun findInterestJournalIds(userId: UUID): Set<UUID> {
        return this.springDataJpaUserJournalInterestRepository
            .findJournalIdsByUserId(userId)
            .toSet()
    }

    override fun findInterestedUserIdsByJournal(userIds: Set<UUID>): Map<UUID, List<UUID>> {
        if (userIds.isEmpty()) return emptyMap()
        return this.springDataJpaUserJournalInterestRepository
            .findByUserIdIn(userIds)
            .groupBy({ it.journalId }, { it.userId })
    }
}
