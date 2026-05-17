package es.uib.record.backend.groups.application.usecase.dto

import es.uib.record.backend.groups.domain.GroupRole
import java.util.UUID

data class GroupSummaryResponseDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val role: GroupRole,
    val memberCount: Int,
    val isOwner: Boolean
)
