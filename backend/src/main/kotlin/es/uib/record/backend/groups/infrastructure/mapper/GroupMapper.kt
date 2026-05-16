package es.uib.record.backend.groups.infrastructure.mapper

import es.uib.record.backend.groups.domain.Group
import es.uib.record.backend.groups.domain.GroupMember
import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupEntity
import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupMemberEntity

fun GroupMember.toEntity() = GroupMemberEntity(
    userId = this.userId,
    role = this.role
)

fun GroupMemberEntity.toDomain() = GroupMember(
    userId = this.userId,
    role = this.role
)

fun Group.toEntity() = GroupEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    members = this.members.map { it.toEntity() }.toMutableSet()
)

fun GroupEntity.toDomain() = Group(
    id = this.id,
    name = this.name,
    description = this.description,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    members = this.members.map { it.toDomain() }
)
