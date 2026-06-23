package es.uib.record.backend.publications.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
class PublicationAuthorEntity(
    @Column(name = "user_id", nullable = false, updatable = false) var userId: UUID
)
