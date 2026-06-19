package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalCategoryQuartileEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaJournalCategoryQuartileRepository :
    JpaRepository<JournalCategoryQuartileEntity, UUID> {
    fun deleteByJournalMetricId(journalMetricId: UUID)
}
