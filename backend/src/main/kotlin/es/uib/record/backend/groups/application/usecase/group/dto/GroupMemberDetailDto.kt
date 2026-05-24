package es.uib.record.backend.groups.application.usecase.group.dto

import es.uib.record.backend.groups.domain.model.GroupRole
import java.util.UUID

data class GroupMemberDetailDto(
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: GroupRole,
    val profileImageUrl: String,
    val isCreator: Boolean,
)
