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
        return this.springDataJpaPublicationRepository.save(publication.toEntity()).toDomain()
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
