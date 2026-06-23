package es.uib.record.backend.publications.infrastructure.persistence.repository

import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaPublicationRepository : JpaRepository<PublicationEntity, UUID> {
    fun findAllByCreatedByOrderByCreatedAtDesc(createdBy: UUID): List<PublicationEntity>
}
