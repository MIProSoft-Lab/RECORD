package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalEntity
import java.time.Instant
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaJournalRepository : JpaRepository<JournalEntity, UUID> {
    fun findByClarivateId(clarivateId: String): JournalEntity?

    @Modifying
    @Query("UPDATE JournalEntity j SET j.lastSyncedAt = :syncedAt WHERE j.id = :id")
    fun markSynced(@Param("id") id: UUID, @Param("syncedAt") syncedAt: Instant)
}
