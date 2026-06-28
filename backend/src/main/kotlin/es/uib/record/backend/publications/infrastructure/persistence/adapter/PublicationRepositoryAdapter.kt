package es.uib.record.backend.publications.infrastructure.persistence.adapter

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.publications.infrastructure.mapper.toDomain
import es.uib.record.backend.publications.infrastructure.mapper.toEntity
import es.uib.record.backend.publications.infrastructure.persistence.repository.SpringDataJpaPublicationRepository
import java.util.UUID
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PublicationRepositoryAdapter(
    private val springDataJpaPublicationRepository: SpringDataJpaPublicationRepository
) : PublicationRepository {

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
}
