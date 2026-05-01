package es.uib.record.backend.auth.domain

import java.util.UUID

data class Token(
    val id: UUID? = null,
    val userId: UUID,
    val token: String,
    val revoked: Boolean = false,
    val expired: Boolean = false,
)