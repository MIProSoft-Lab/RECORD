package es.uib.record.backend.publications.infrastructure.mapper

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationAuthor
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationAuthorEntity
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationEntity

fun PublicationAuthor.toEntity() = PublicationAuthorEntity(userId = this.userId)

fun PublicationAuthorEntity.toDomain() = PublicationAuthor(userId = this.userId)

fun Publication.toEntity() =
    PublicationEntity(
        id = this.id,
        title = this.title,
        abstractText = this.abstractText,
        doi = this.doi,
        journalId = this.journalId,
        groupId = this.groupId,
        status = this.status,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
        authors = this.authors.map { it.toEntity() }.toMutableSet(),
    )

fun PublicationEntity.toDomain() =
    Publication(
        id = this.id,
        title = this.title,
        abstractText = this.abstractText,
        doi = this.doi,
        journalId = this.journalId,
        groupId = this.groupId,
        status = this.status,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
        authors = this.authors.map { it.toDomain() },
    )
