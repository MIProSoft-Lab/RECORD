package es.uib.record.backend.publications.domain.repository

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.shared.domain.PageResult
import java.time.Instant
import java.util.UUID

interface PublicationRepository {
    fun save(publication: Publication): Publication

    fun findById(id: UUID): Publication?

    fun findAllByCreatedBy(createdBy: UUID): List<Publication>

    fun findAllByAuthor(userId: UUID): List<Publication>

    /**
     * Página del historial de publicaciones en las que [userId] figura como autor, ordenada por
     * fecha de creación descendente. Todos los filtros son opcionales (null = sin filtro):
     * - [title]: búsqueda parcial e insensible a mayúsculas por título.
     * - [status]: estado exacto del ciclo de vida.
     * - [journalId]: journal asociado.
     * - [staleBefore]: solo publicaciones cuyo último cambio de estado es anterior o igual a este
     *   instante (es decir, llevan "estancadas" desde antes de él).
     * - [excludeFinalStatuses]: cuando es `true`, excluye las publicaciones en estado final.
     * - [onlyMainAuthor]: cuando es `true`, solo aquellas en las que [userId] es el primer autor.
     */
    fun searchByAuthor(
        userId: UUID,
        title: String?,
        status: PublicationStatus?,
        journalId: UUID?,
        staleBefore: Instant?,
        excludeFinalStatuses: Boolean,
        onlyMainAuthor: Boolean,
        page: Int,
        size: Int,
    ): PageResult<Publication>

    /**
     * Página de las publicaciones que pertenecen al grupo [groupId], ordenada por fecha de creación
     * descendente. Cuando [authorIds] es `null` se incluyen todas las publicaciones del grupo;
     * cuando trae una lista, solo las publicaciones en las que alguno de esos usuarios figura como
     * autor (creador o co-autor). El resto de filtros se comportan igual que en [searchByAuthor].
     */
    fun searchByGroup(
        groupId: UUID,
        authorIds: List<UUID>?,
        title: String?,
        status: PublicationStatus?,
        journalId: UUID?,
        staleBefore: Instant?,
        excludeFinalStatuses: Boolean,
        page: Int,
        size: Int,
    ): PageResult<Publication>

    fun delete(id: UUID)
}
