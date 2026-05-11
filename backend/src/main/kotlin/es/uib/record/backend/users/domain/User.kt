package es.uib.record.backend.users.domain

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID? = null,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val pushNotifications: Boolean = true,
    val profileImageUrl: String? = null,
    val createdAt: Instant = Instant.now(),
)