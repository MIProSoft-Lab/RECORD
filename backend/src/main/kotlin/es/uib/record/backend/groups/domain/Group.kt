package es.uib.record.backend.groups.domain

import java.time.Instant
import java.util.UUID

class Group (
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    val createdBy: UUID,
    val createdAt: Instant = Instant.now(),
    members: List<GroupMember> = emptyList()
) {
    private val _members: MutableList<GroupMember> = members.toMutableList()

    val members: List<GroupMember>
        get() = _members.toList()

    fun addMember(userId: UUID, role: GroupRole = GroupRole.MEMBER) {
        _members.add(GroupMember(userId, role))
    }
}