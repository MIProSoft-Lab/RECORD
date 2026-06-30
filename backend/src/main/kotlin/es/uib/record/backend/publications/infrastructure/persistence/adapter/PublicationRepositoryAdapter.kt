package es.uib.record.backend.publications.infrastructure.persistence.adapter

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.publications.infrastructure.mapper.toDomain
import es.uib.record.backend.publications.infrastructure.mapper.toEntity
import es.uib.record.backend.publications.infrastructure.persistence.repository.SpringDataJpaPublicationRepository
import es.uib.record.backend.shared.domain.PageResult
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PublicationRepositoryAdapter(
    private val springDataJpaPublicationRepository: SpringDataJpaPublicationRepository
) : PublicationRepository {

    private companion object {
        // Centinela usado cuando no hay filtro de "días en estado": ningún cambio de estado es
        // posterior a este instante, así que la condición de estancamiento se cumple siempre.
        val NO_STALE_FILTER: Instant = Instant.parse("9999-01-01T00:00:00Z")
    }

    // El borrado de autores y la reinserción deben ser atómicos: si no, un fallo a mitad
    // dejaría la publicación sin autores (y, al filtrarse el listado por autor, desaparecería).
    @Transactional
    override fun save(publication: Publication): Publication {
        val entity = publication.toEntity()
        // En actualizaciones (id presente) se vacían primero los autores existentes y se
        // fuerza el flush antes de reinsertar los nuevos. Así se evita colisionar con la
        // restricción única (publication_id, user_id) al conservar autores internos —p. ej.
        // el creador—, ya que el borrado de la fila antigua ocurre antes de la nueva inserción.
        val id = entity.id
        if (id != null) {
            this.springDataJpaPublicationRepository.findById(id).ifPresent { existing ->
                existing.authors.clear()
                existing.statusHistory.clear()
                this.springDataJpaPublicationRepository.saveAndFlush(existing)
            }
            // Los autores entrantes pueden conservar el id de las filas recién borradas (p. ej.
            // al cambiar solo el estado preservando los autores). Se reinsertan siempre como
            // filas nuevas para que sean INSERT y no un merge sobre una fila inexistente
            // (StaleObjectStateException). El historial se reinserta por el mismo motivo.
            entity.authors.forEach { it.id = null }
            entity.statusHistory.forEach { it.id = null }
        }
        return this.springDataJpaPublicationRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Publication? {
        return this.springDataJpaPublicationRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findAllByCreatedBy(createdBy: UUID): List<Publication> {
        return this.springDataJpaPublicationRepository
            .findAllByCreatedByOrderByCreatedAtDesc(createdBy)
            .map { it.toDomain() }
    }

    override fun findAllByAuthor(userId: UUID): List<Publication> {
        return this.springDataJpaPublicationRepository
            .findAllByAuthors_UserIdOrderByCreatedAtDesc(userId)
            .map { it.toDomain() }
    }

    override fun searchByAuthor(
        userId: UUID,
        title: String?,
        status: PublicationStatus?,
        journalId: UUID?,
        staleBefore: Instant?,
        excludeFinalStatuses: Boolean,
        onlyMainAuthor: Boolean,
        page: Int,
        size: Int,
    ): PageResult<Publication> {
        val titlePattern = title?.let { "%${it.lowercase()}%" }
        val idPage =
            this.springDataJpaPublicationRepository.searchPublicationIds(
                userId = userId,
                titlePattern = titlePattern,
                status = status,
                journalId = journalId,
                staleBefore = staleBefore ?: NO_STALE_FILTER,
                excludeFinalStatuses = excludeFinalStatuses,
                onlyMainAuthor = onlyMainAuthor,
                pageable = PageRequest.of(page, size),
            )

        return this.hydratePage(idPage, page, size)
    }

    override fun searchByGroup(
        groupId: UUID,
        authorIds: List<UUID>?,
        title: String?,
        status: PublicationStatus?,
        journalId: UUID?,
        staleBefore: Instant?,
        excludeFinalStatuses: Boolean,
        page: Int,
        size: Int,
    ): PageResult<Publication> {
        val titlePattern = title?.let { "%${it.lowercase()}%" }
        val pageable = PageRequest.of(page, size)

        val idPage =
            if (authorIds == null) {
                this.springDataJpaPublicationRepository.searchGroupPublicationIds(
                    groupId = groupId,
                    titlePattern = titlePattern,
                    status = status,
                    journalId = journalId,
                    staleBefore = staleBefore ?: NO_STALE_FILTER,
                    excludeFinalStatuses = excludeFinalStatuses,
                    pageable = pageable,
                )
            } else {
                this.springDataJpaPublicationRepository.searchGroupPublicationIdsByAuthors(
                    groupId = groupId,
                    authorIds = authorIds,
                    titlePattern = titlePattern,
                    status = status,
                    journalId = journalId,
                    staleBefore = staleBefore ?: NO_STALE_FILTER,
                    excludeFinalStatuses = excludeFinalStatuses,
                    pageable = pageable,
                )
            }

        return this.hydratePage(idPage, page, size)
    }

    // findAllById no garantiza el orden; se reordena según la página de IDs (ya ordenada por
    // fecha de creación descendente) para preservar el orden devuelto por la consulta.
    private fun hydratePage(
        idPage: org.springframework.data.domain.Page<UUID>,
        page: Int,
        size: Int,
    ): PageResult<Publication> {
        val ids = idPage.content
        if (ids.isEmpty()) return PageResult(emptyList(), idPage.totalElements, page, size)

        val publicationsById =
            this.springDataJpaPublicationRepository.findAllById(ids).associateBy { it.id }
        val items = ids.mapNotNull { id -> publicationsById[id]?.toDomain() }

        return PageResult(items, idPage.totalElements, page, size)
    }

    // El borrado es definitivo. Las filas de autores e historial se eliminan en cascada
    // (ON DELETE CASCADE en publication_authors y publication_status_history).
    @Transactional
    override fun delete(id: UUID) {
        this.springDataJpaPublicationRepository.deleteById(id)
    }
}
