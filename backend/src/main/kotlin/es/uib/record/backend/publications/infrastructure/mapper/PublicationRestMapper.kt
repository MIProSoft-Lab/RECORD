package es.uib.record.backend.publications.infrastructure.mapper

import es.uib.record.backend.model.CreatePublicationRequest
import es.uib.record.backend.model.GroupPublicationListPageResponse
import es.uib.record.backend.model.GroupPublicationSummaryResponse
import es.uib.record.backend.model.PublicationAuthorInput
import es.uib.record.backend.model.PublicationAuthorResponse
import es.uib.record.backend.model.PublicationAuthorType as ApiPublicationAuthorType
import es.uib.record.backend.model.PublicationCreatorResponse
import es.uib.record.backend.model.PublicationListPageResponse
import es.uib.record.backend.model.PublicationResponse
import es.uib.record.backend.model.PublicationStatus as ApiPublicationStatus
import es.uib.record.backend.model.PublicationStatusHistoryEntry
import es.uib.record.backend.model.PublicationSummaryResponse
import es.uib.record.backend.model.UpdatePublicationRequest
import es.uib.record.backend.publications.application.usecase.dto.CreatePublicationRequestDto
import es.uib.record.backend.publications.application.usecase.dto.GroupPublicationSummaryDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorInputDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorType
import es.uib.record.backend.publications.application.usecase.dto.PublicationCreatorDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationStatusHistoryDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationSummaryDto
import es.uib.record.backend.publications.application.usecase.dto.UpdatePublicationRequestDto
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.shared.domain.PageResult
import java.time.ZoneOffset

fun CreatePublicationRequest.toDto() =
    CreatePublicationRequestDto(
        title = this.title,
        abstractText = this.`abstract`,
        doi = this.doi,
        journalId = this.journalId,
        groupId = this.groupId,
        status = this.status?.toDomain(),
        authors = (this.authors ?: emptyList()).map { it.toDto() },
    )

fun UpdatePublicationRequest.toDto() =
    UpdatePublicationRequestDto(
        title = this.title,
        abstractText = this.`abstract`,
        doi = this.doi,
        authors = (this.authors ?: emptyList()).map { it.toDto() },
    )

fun PublicationAuthorInput.toDto() =
    PublicationAuthorInputDto(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
    )

fun PublicationAuthorDto.toResponse() =
    PublicationAuthorResponse(
        authorId = this.authorId,
        type = this.type.toResponse(),
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        profileImageUrl = this.profileImageUrl,
    )

fun PublicationAuthorType.toResponse(): ApiPublicationAuthorType =
    when (this) {
        PublicationAuthorType.INTERNAL -> ApiPublicationAuthorType.INTERNAL
        PublicationAuthorType.EXTERNAL -> ApiPublicationAuthorType.EXTERNAL
    }

fun PublicationDetailDto.toResponse() =
    PublicationResponse(
        id = this.id,
        title = this.title,
        groupId = this.groupId,
        journalId = this.journalId,
        status = this.status.toResponse(),
        createdBy = this.createdBy,
        createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
        statusHistory = this.statusHistory.map { it.toResponse() },
        journalName = this.journalName,
        abstract = this.abstractText,
        doi = this.doi,
        authors = this.authors.map { it.toResponse() },
    )

fun PublicationStatusHistoryDto.toResponse() =
    PublicationStatusHistoryEntry(
        status = this.status.toResponse(),
        journalId = this.journalId,
        changedAt = this.changedAt.atOffset(ZoneOffset.UTC),
        journalName = this.journalName,
        comment = this.comment,
    )

fun PageResult<PublicationSummaryDto>.toResponse() =
    PublicationListPageResponse(
        content = this.items.map { it.toResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        page = this.page,
        propertySize = this.size,
    )

fun PublicationSummaryDto.toResponse() =
    PublicationSummaryResponse(
        id = this.id,
        title = this.title,
        groupId = this.groupId,
        journalId = this.journalId,
        status = this.status.toResponse(),
        createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
        statusChangedAt = this.statusChangedAt.atOffset(ZoneOffset.UTC),
        journalName = this.journalName,
    )

fun PageResult<GroupPublicationSummaryDto>.toGroupResponse() =
    GroupPublicationListPageResponse(
        content = this.items.map { it.toResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        page = this.page,
        propertySize = this.size,
    )

fun GroupPublicationSummaryDto.toResponse() =
    GroupPublicationSummaryResponse(
        id = this.id,
        title = this.title,
        groupId = this.groupId,
        journalId = this.journalId,
        status = this.status.toResponse(),
        createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
        statusChangedAt = this.statusChangedAt.atOffset(ZoneOffset.UTC),
        creator = this.creator.toResponse(),
        journalName = this.journalName,
    )

fun PublicationCreatorDto.toResponse() =
    PublicationCreatorResponse(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        profileImageUrl = this.profileImageUrl,
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
