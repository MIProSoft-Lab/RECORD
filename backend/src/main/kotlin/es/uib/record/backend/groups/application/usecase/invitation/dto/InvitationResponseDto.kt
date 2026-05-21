package es.uib.record.backend.groups.application.usecase.invitation.dto

import java.time.Instant
import java.util.UUID

data class InvitationResponseDto(
    val id: UUID,
    val group: GroupInvitationSummaryDto,
    val inviter: InviterSummaryDto,
    val createdAt: Instant,
)
