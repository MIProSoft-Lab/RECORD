package es.uib.record.backend.groups.application.usecase.group.dto

import java.util.UUID

data class InvitableUserResponseDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: String,
)
