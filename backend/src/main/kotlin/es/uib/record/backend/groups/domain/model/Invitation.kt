package es.uib.record.backend.groups.domain.model

import java.time.Instant
import java.util.UUID

data class Invitation(
    val id: UUID? = null,
    val groupId: UUID,
    val inviteeUserId: UUID,
    val inviterUserId: UUID,
    val createdAt: Instant = Instant.now(),
)
