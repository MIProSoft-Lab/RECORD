package es.uib.record.backend.groups.domain.repository

import es.uib.record.backend.groups.domain.model.Invitation
import java.util.UUID

interface InvitationRepository {
    fun save(invitation: Invitation): Invitation
    fun delete(invitation: Invitation)
    fun findByInviteeUserId(userId: UUID): List<Invitation>
    fun findById(id: UUID): Invitation?
}