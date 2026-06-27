package es.uib.record.backend.publications.infrastructure.persistence.adapter

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.publications.infrastructure.mapper.toDomain
import es.uib.record.backend.publications.infrastructure.mapper.toEntity
import es.uib.record.backend.publications.infrastructure.persistence.repository.SpringDataJpaPublicationRepository
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class PublicationRepositoryAdapter(
    private val springDataJpaPublicationRepository: SpringDataJpaPublicationRepository
) : PublicationRepository {

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
                this.springDataJpaPublicationRepository.saveAndFlush(existing)
            }
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
