package es.uib.record.backend.journals.domain.repository

import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalDetail
import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.shared.domain.PageResult
import java.time.Instant
import java.util.UUID

interface JournalRepository {
    fun save(journal: Journal): Journal

    fun findByClarivateId(clarivateId: String): Journal?

    fun markSynced(id: UUID, syncedAt: Instant)

    /**
     * Búsqueda paginada de revistas. Todos los filtros son opcionales y combinables; [categoryId] y
     * [quartile] se aplican sobre el último año disponible de cada revista.
     */
    fun search(
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): PageResult<JournalSearchItem>

    /**
     * Búsqueda paginada restringida a las revistas que [userId] ha marcado como de interés. Mismos
     * filtros que [search]; todos los resultados tienen `isInterest = true`.
     */
    fun searchInterests(
        userId: UUID,
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): PageResult<JournalSearchItem>

    fun existsById(id: UUID): Boolean

    fun findDetailById(id: UUID): JournalDetail?
}
