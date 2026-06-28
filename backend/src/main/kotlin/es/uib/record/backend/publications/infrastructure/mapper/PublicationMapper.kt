package es.uib.record.backend.publications.infrastructure.mapper

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationAuthor
import es.uib.record.backend.publications.domain.model.PublicationStatusHistoryEntry
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationAuthorEntity
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationEntity
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationStatusHistoryEntity

fun PublicationAuthor.toEntity(position: Int): PublicationAuthorEntity =
    when (this) {
        is PublicationAuthor.InternalAuthor ->
            PublicationAuthorEntity(id = this.id, userId = this.userId, position = position)
        is PublicationAuthor.ExternalAuthor ->
            PublicationAuthorEntity(
                id = this.id,
                firstName = this.firstName,
                lastName = this.lastName,
                position = position,
            )
    }

fun PublicationAuthorEntity.toDomain(): PublicationAuthor =
    if (this.userId != null) {
        PublicationAuthor.InternalAuthor(userId = this.userId!!, id = this.id)
    } else {
        PublicationAuthor.ExternalAuthor(
            firstName = this.firstName!!,
            lastName = this.lastName!!,
            id = this.id,
        )
    }

fun PublicationStatusHistoryEntry.toEntity(position: Int): PublicationStatusHistoryEntity =
    PublicationStatusHistoryEntity(
        status = this.status,
        journalId = this.journalId,
        changedAt = this.changedAt,
        comment = this.comment,
        position = position,
    )

fun PublicationStatusHistoryEntity.toDomain(): PublicationStatusHistoryEntry =
    PublicationStatusHistoryEntry(
        status = this.status,
        journalId = this.journalId,
        changedAt = this.changedAt,
        comment = this.comment,
    )

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
        authors = this.authors.mapIndexed { index, author -> author.toEntity(index) }.toMutableList(),
        statusHistory =
            this.statusHistory.mapIndexed { index, entry -> entry.toEntity(index) }.toMutableList(),
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
        statusHistory = this.statusHistory.map { it.toDomain() },
    )
