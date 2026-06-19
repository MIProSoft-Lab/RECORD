package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.UserJournalInterestEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaUserJournalInterestRepository :
    JpaRepository<UserJournalInterestEntity, UUID> {

    fun existsByUserIdAndJournalId(userId: UUID, journalId: UUID): Boolean

    @Modifying
    fun deleteByUserIdAndJournalId(userId: UUID, journalId: UUID)

    @Query(
        "SELECT uji.journalId FROM UserJournalInterestEntity uji WHERE uji.userId = :userId"
    )
    fun findJournalIdsByUserId(@Param("userId") userId: UUID): List<UUID>
}
