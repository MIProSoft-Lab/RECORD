package es.uib.record.backend.groups.application.usecase.invitation.dto

import java.util.UUID

data class InviterSummaryDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val profileImageUrl: String,
)
