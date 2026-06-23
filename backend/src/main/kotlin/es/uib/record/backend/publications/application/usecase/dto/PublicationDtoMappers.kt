package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.Publication

fun Publication.toDetailDto(journalName: String?) =
    PublicationDetailDto(
        id = this.id!!,
        title = this.title,
        abstractText = this.abstractText,
        doi = this.doi,
        journalId = this.journalId,
        journalName = journalName,
        groupId = this.groupId,
        status = this.status,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
    )

fun Publication.toSummaryDto(journalName: String?) =
    PublicationSummaryDto(
        id = this.id!!,
        title = this.title,
        journalId = this.journalId,
        journalName = journalName,
        groupId = this.groupId,
        status = this.status,
        createdAt = this.createdAt,
    )
