package es.uib.record.backend.groups.domain.model

import java.util.UUID

data class GroupMember(val userId: UUID, val role: GroupRole)
