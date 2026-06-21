package es.uib.record.backend.groups.infrastructure.mapper

import es.uib.record.backend.groups.application.usecase.group.dto.CreateGroupRequestDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupDetailResponseDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupJournalInterestCategoryDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupJournalInterestMemberDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupJournalInterestResponseDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupMemberDetailDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupSummaryResponseDto
import es.uib.record.backend.groups.application.usecase.group.dto.InvitableUserResponseDto
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.model.CreateGroupRequest
import es.uib.record.backend.model.GroupDetailResponse
import es.uib.record.backend.model.GroupJournalInterestMember
import es.uib.record.backend.model.GroupJournalInterestPageResponse
import es.uib.record.backend.model.GroupJournalInterestResponse
import es.uib.record.backend.model.GroupMemberDetail
import es.uib.record.backend.model.GroupResponse
import es.uib.record.backend.model.GroupSummaryResponse
import es.uib.record.backend.model.InvitableUserResponse
import es.uib.record.backend.model.JournalCategoryQuartileSummary
import es.uib.record.backend.model.Quartile as ApiQuartile
import es.uib.record.backend.shared.domain.PageResult
import java.time.ZoneOffset

fun CreateGroupRequest.toDto() =
    CreateGroupRequestDto(name = this.name, description = this.description)

fun Group.toResponse() =
    GroupResponse(
        id = this.id!!,
        name = this.name,
        description = this.description,
        createdBy = this.createdBy,
    )

fun GroupSummaryResponseDto.toResponse() =
    GroupSummaryResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        role = this.role.toResponse(),
        memberCount = this.memberCount,
        isOwner = this.isOwner,
    )

fun GroupRole.toResponse() =
    when (this) {
        GroupRole.MEMBER -> es.uib.record.backend.model.GroupRole.MEMBER
        GroupRole.ADMIN -> es.uib.record.backend.model.GroupRole.ADMIN
    }

fun es.uib.record.backend.model.GroupRole.toDomain() =
    when (this) {
        es.uib.record.backend.model.GroupRole.MEMBER -> GroupRole.MEMBER
        es.uib.record.backend.model.GroupRole.ADMIN -> GroupRole.ADMIN
    }

fun GroupDetailResponseDto.toResponse() =
    GroupDetailResponse(
        id = this.groupId,
        name = this.name,
        description = this.description,
        createdBy = this.createdBy,
        members = this.members.map(GroupMemberDetailDto::toResponse),
        createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
    )

fun GroupMemberDetailDto.toResponse() =
    GroupMemberDetail(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        role = this.role.toResponse(),
        isCreator = this.isCreator,
        profileImageUrl = this.profileImageUrl,
    )

fun InvitableUserResponseDto.toResponse() =
    InvitableUserResponse(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        profileImageUrl = this.profileImageUrl,
    )

fun PageResult<GroupJournalInterestResponseDto>.toResponse() =
    GroupJournalInterestPageResponse(
        content = this.items.map { it.toResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        page = this.page,
        propertySize = this.size,
    )

fun GroupJournalInterestResponseDto.toResponse() =
    GroupJournalInterestResponse(
        id = this.id,
        name = this.name,
        categories = this.categories.map { it.toResponse() },
        favoriteCount = this.favoriteCount,
        members = this.members.map { it.toResponse() },
        issn = this.issn,
        eIssn = this.eIssn,
        publisherName = this.publisherName,
        year = this.year,
    )

fun GroupJournalInterestCategoryDto.toResponse() =
    JournalCategoryQuartileSummary(
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        quartile = ApiQuartile.valueOf(this.quartile),
        edition = this.edition,
        impactFactor = this.impactFactor,
    )

fun GroupJournalInterestMemberDto.toResponse() =
    GroupJournalInterestMember(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        profileImageUrl = this.profileImageUrl,
    )
