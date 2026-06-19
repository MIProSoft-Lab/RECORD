package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalSyncStateEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaJournalSyncStateRepository : JpaRepository<JournalSyncStateEntity, UUID>
