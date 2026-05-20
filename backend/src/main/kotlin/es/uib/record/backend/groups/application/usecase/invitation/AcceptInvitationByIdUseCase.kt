package es.uib.record.backend.groups.application.usecase.invitation

import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.InvitationDoesNotBelongToTheUserException
import es.uib.record.backend.groups.domain.exception.InvitationNotFoundException
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AcceptInvitationByIdUseCase(
    private val groupRepository: GroupRepository,
    private val invitationRepository: InvitationRepository,
    private val userFacade: UserFacade
) {
    @Transactional
    fun execute(invitationId: UUID, email: String) {
        val invitation = this.invitationRepository.findById(invitationId)
            ?: throw InvitationNotFoundException(invitationId)
        val userId = this.userFacade.getUserIdByEmail(email)

        if (invitation.inviteeUserId != userId)
            throw InvitationDoesNotBelongToTheUserException()

        val group = this.groupRepository.findById(invitation.groupId)
            ?: throw GroupNotFoundException(invitation.groupId)

        group.addMember(userId)
        this.groupRepository.save(group)
        this.invitationRepository.delete(invitation)
    }
}