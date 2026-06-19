package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalEntity
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaJournalRepository : JpaRepository<JournalEntity, UUID> {
    fun findByClarivateId(clarivateId: String): JournalEntity?

    @Modifying
    @Query("UPDATE JournalEntity j SET j.lastSyncedAt = :syncedAt WHERE j.id = :id")
    fun markSynced(@Param("id") id: UUID, @Param("syncedAt") syncedAt: Instant)

    /**
     * IDs de las revistas que cumplen los filtros, paginadas y ordenadas por nombre. Los filtros de
     * categoría/cuartil se aplican sobre el último año disponible de cada revista. Si no hay filtro
     * de categoría ni cuartil, se devuelven todas las revistas que casan por nombre.
     *
     * [namePattern] llega ya en minúsculas y con comodines (`%texto%`), o `null`. Se usa solo como
     * operando de `LIKE` (no envuelto en `LOWER(...)`) para que Postgres infiera el tipo `text` del
     * parámetro incluso cuando es `null` — de lo contrario `LOWER(:null)` falla con `lower(bytea)`.
     */
    @Query(
        value =
            "SELECT j.id FROM JournalEntity j " +
                "WHERE (:namePattern IS NULL OR LOWER(j.name) LIKE :namePattern) " +
                "AND ((:categoryId IS NULL AND :quartile IS NULL) OR EXISTS (" +
                "  SELECT 1 FROM JournalCategoryQuartileEntity jcq " +
                "  WHERE jcq.journalId = j.id " +
                "  AND jcq.year = (SELECT MAX(jcq2.year) FROM JournalCategoryQuartileEntity jcq2 WHERE jcq2.journalId = j.id) " +
                "  AND (:categoryId IS NULL OR jcq.categoryId = :categoryId) " +
                "  AND (:quartile IS NULL OR jcq.quartile = :quartile))) " +
                "ORDER BY j.name",
        countQuery =
            "SELECT COUNT(j.id) FROM JournalEntity j " +
                "WHERE (:namePattern IS NULL OR LOWER(j.name) LIKE :namePattern) " +
                "AND ((:categoryId IS NULL AND :quartile IS NULL) OR EXISTS (" +
                "  SELECT 1 FROM JournalCategoryQuartileEntity jcq " +
                "  WHERE jcq.journalId = j.id " +
                "  AND jcq.year = (SELECT MAX(jcq2.year) FROM JournalCategoryQuartileEntity jcq2 WHERE jcq2.journalId = j.id) " +
                "  AND (:categoryId IS NULL OR jcq.categoryId = :categoryId) " +
                "  AND (:quartile IS NULL OR jcq.quartile = :quartile)))",
    )
    fun searchJournalIds(
        @Param("namePattern") namePattern: String?,
        @Param("categoryId") categoryId: UUID?,
        @Param("quartile") quartile: Quartile?,
        pageable: Pageable,
    ): Page<UUID>
}
