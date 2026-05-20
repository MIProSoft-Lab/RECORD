package es.uib.record.backend.groups.infrastructure.mapper

import es.uib.record.backend.groups.application.usecase.invitation.dto.GroupInvitationSummaryDto
import es.uib.record.backend.groups.application.usecase.invitation.dto.InvitationResponseDto
import es.uib.record.backend.groups.application.usecase.invitation.dto.InviterSummaryDto
import es.uib.record.backend.model.GroupInvitationSummary
import es.uib.record.backend.model.InvitationResponse
import es.uib.record.backend.model.InviterSummary
import java.time.ZoneOffset

fun InvitationResponseDto.toResponse() = InvitationResponse(
    this.id,
    this.group.toGroupSummary(),
    this.inviter.toInviterSummary(),
    this.createdAt.atOffset(ZoneOffset.UTC)
)

fun GroupInvitationSummaryDto.toGroupSummary() = GroupInvitationSummary(
    this.id,
    this.groupName
)

fun InviterSummaryDto.toInviterSummary() = InviterSummary(
    this.id,
    this.firstName,
    this.lastName,
    this.profileImageUrl
)