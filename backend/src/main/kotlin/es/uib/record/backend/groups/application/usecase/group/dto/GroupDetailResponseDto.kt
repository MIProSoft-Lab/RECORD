package es.uib.record.backend.groups.application.usecase.group.dto

import java.time.Instant
import java.util.UUID

data class GroupDetailResponseDto(
    val groupId: UUID,
    val name: String,
    val description: String?,
    val createdBy: UUID,
    val createdAt: Instant,
    val members: List<GroupMemberDetailDto>,
)
