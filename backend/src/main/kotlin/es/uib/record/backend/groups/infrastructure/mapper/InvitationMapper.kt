package es.uib.record.backend.groups.infrastructure.mapper

import es.uib.record.backend.groups.domain.model.Invitation
import es.uib.record.backend.groups.infrastructure.persistence.entity.InvitationEntity

fun Invitation.toEntity() =
    InvitationEntity(this.id, this.groupId, this.inviteeUserId, this.inviterUserId, this.createdAt)

fun InvitationEntity.toDomain() =
    Invitation(this.id, this.groupId, this.inviteeUserId, this.inviterUserId, this.createdAt)
