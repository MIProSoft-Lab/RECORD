package es.uib.record.backend.groups.infrastructure.mapper

import es.uib.record.backend.groups.application.usecase.dto.CreateGroupRequestDto
import es.uib.record.backend.groups.application.usecase.dto.GroupDetailResponseDto
import es.uib.record.backend.groups.application.usecase.dto.GroupMemberDetailDto
import es.uib.record.backend.groups.application.usecase.dto.GroupSummaryResponseDto
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.model.CreateGroupRequest
import es.uib.record.backend.model.GroupDetailResponse
import es.uib.record.backend.model.GroupMemberDetail
import es.uib.record.backend.model.GroupResponse
import es.uib.record.backend.model.GroupSummaryResponse
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
