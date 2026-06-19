package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalMetricEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaJournalMetricRepository : JpaRepository<JournalMetricEntity, UUID> {
    fun findByJournalIdAndYear(journalId: UUID, year: Int): JournalMetricEntity?

    fun deleteByJournalIdAndYearNotIn(journalId: UUID, years: List<Int>)

    fun findByJournalIdOrderByYearDesc(journalId: UUID): List<JournalMetricEntity>
}
