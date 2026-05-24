package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.AlreadyGroupMemberException
import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.exception.UserAlreadyInvitedException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.model.Invitation
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class SendInvitationUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade,
    private val invitationRepository: InvitationRepository,
) {
    fun execute(email: String, groupId: UUID, inviteeUserId: UUID) {
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val inviterUserId = this.userFacade.getUserIdByEmail(email)

        if (!group.isMember(inviterUserId)) throw NotGroupMemberException(inviterUserId, groupId)

        if (group.getMemberRole(inviterUserId) != GroupRole.ADMIN)
            throw GroupMemberNotAdminException(inviterUserId, groupId)

        this.userFacade.getUserById(inviteeUserId)

        if (group.isMember(inviteeUserId)) throw AlreadyGroupMemberException(inviteeUserId, groupId)

        val groupPendingInvitations = this.invitationRepository.findByGroupId(groupId)
        if (groupPendingInvitations.any { it.inviteeUserId == inviteeUserId })
            throw UserAlreadyInvitedException(inviteeUserId, groupId)

        this.invitationRepository.save(
            Invitation(
                groupId = groupId,
                inviteeUserId = inviteeUserId,
                inviterUserId = inviterUserId,
            )
        )
    }
}
