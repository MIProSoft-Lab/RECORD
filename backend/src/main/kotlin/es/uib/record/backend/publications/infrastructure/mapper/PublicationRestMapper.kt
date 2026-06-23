package es.uib.record.backend.publications.infrastructure.mapper

import es.uib.record.backend.model.CreatePublicationRequest
import es.uib.record.backend.model.PublicationResponse
import es.uib.record.backend.model.PublicationSummaryResponse
import es.uib.record.backend.publications.application.usecase.dto.CreatePublicationRequestDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationSummaryDto
import es.uib.record.backend.publications.domain.model.PublicationStatus
import java.time.ZoneOffset
import es.uib.record.backend.model.PublicationStatus as ApiPublicationStatus

fun CreatePublicationRequest.toDto() =
    CreatePublicationRequestDto(
        title = this.title,
        abstractText = this.`abstract`,
        doi = this.doi,
        journalId = this.journalId,
        groupId = this.groupId,
        status = this.status?.toDomain(),
    )

fun PublicationDetailDto.toResponse() =
    PublicationResponse(
        id = this.id,
        title = this.title,
        groupId = this.groupId,
        journalId = this.journalId,
        status = this.status.toResponse(),
        createdBy = this.createdBy,
        createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
        journalName = this.journalName,
        abstract = this.abstractText,
        doi = this.doi,
    )

fun PublicationSummaryDto.toResponse() =
    PublicationSummaryResponse(
        id = this.id,
        title = this.title,
        groupId = this.groupId,
        journalId = this.journalId,
        status = this.status.toResponse(),
        createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
        journalName = this.journalName,
    )

fun PublicationStatus.toResponse(): ApiPublicationStatus =
    when (this) {
        PublicationStatus.PLANNED -> ApiPublicationStatus.PLANNED
        PublicationStatus.SUBMITTED -> ApiPublicationStatus.SUBMITTED
        PublicationStatus.UNDER_REVIEW -> ApiPublicationStatus.UNDER_REVIEW
        PublicationStatus.MINOR_REVISION -> ApiPublicationStatus.MINOR_REVISION
        PublicationStatus.MAJOR_REVISION -> ApiPublicationStatus.MAJOR_REVISION
        PublicationStatus.REJECTED -> ApiPublicationStatus.REJECTED
        PublicationStatus.ACCEPTED -> ApiPublicationStatus.ACCEPTED
        PublicationStatus.PUBLISHED -> ApiPublicationStatus.PUBLISHED
    }

fun ApiPublicationStatus.toDomain(): PublicationStatus =
    when (this) {
        ApiPublicationStatus.PLANNED -> PublicationStatus.PLANNED
        ApiPublicationStatus.SUBMITTED -> PublicationStatus.SUBMITTED
        ApiPublicationStatus.UNDER_REVIEW -> PublicationStatus.UNDER_REVIEW
        ApiPublicationStatus.MINOR_REVISION -> PublicationStatus.MINOR_REVISION
        ApiPublicationStatus.MAJOR_REVISION -> PublicationStatus.MAJOR_REVISION
        ApiPublicationStatus.REJECTED -> PublicationStatus.REJECTED
        ApiPublicationStatus.ACCEPTED -> PublicationStatus.ACCEPTED
        ApiPublicationStatus.PUBLISHED -> PublicationStatus.PUBLISHED
    }
