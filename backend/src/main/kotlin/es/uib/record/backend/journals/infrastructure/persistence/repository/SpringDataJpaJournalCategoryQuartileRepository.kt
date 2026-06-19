package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalCategoryQuartileEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaJournalCategoryQuartileRepository :
    JpaRepository<JournalCategoryQuartileEntity, UUID> {
    fun deleteByJournalMetricId(journalMetricId: UUID)

    /** Cuartiles del último año disponible de cada una de las revistas indicadas, con su categoría. */
    @Query(
        "SELECT new es.uib.record.backend.journals.infrastructure.persistence.repository.JournalCategoryQuartileView(" +
            "jcq.journalId, jcq.categoryId, c.name, c.edition, jcq.year, jcq.quartile, jcq.impactFactor) " +
            "FROM JournalCategoryQuartileEntity jcq, CategoryEntity c " +
            "WHERE jcq.categoryId = c.id AND jcq.journalId IN :journalIds " +
            "AND jcq.year = (SELECT MAX(jcq2.year) FROM JournalCategoryQuartileEntity jcq2 WHERE jcq2.journalId = jcq.journalId) " +
            "ORDER BY c.name"
    )
    fun findLatestYearViews(
        @Param("journalIds") journalIds: Collection<UUID>
    ): List<JournalCategoryQuartileView>

    /** Todos los cuartiles de una revista (todos los años), con su categoría, para la vista de detalle. */
    @Query(
        "SELECT new es.uib.record.backend.journals.infrastructure.persistence.repository.JournalCategoryQuartileView(" +
            "jcq.journalId, jcq.categoryId, c.name, c.edition, jcq.year, jcq.quartile, jcq.impactFactor) " +
            "FROM JournalCategoryQuartileEntity jcq, CategoryEntity c " +
            "WHERE jcq.categoryId = c.id AND jcq.journalId = :journalId " +
            "ORDER BY jcq.year DESC, c.name"
    )
    fun findViewsByJournalId(@Param("journalId") journalId: UUID): List<JournalCategoryQuartileView>
}
