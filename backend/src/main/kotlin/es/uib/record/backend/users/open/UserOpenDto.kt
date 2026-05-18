package es.uib.record.backend.users.open

import java.util.UUID

data class UserOpenDto(
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: String?,
)
