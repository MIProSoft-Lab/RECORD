package es.uib.record.backend.groups.application.usecase.invitation

import es.uib.record.backend.groups.domain.exception.InvitationDoesNotBelongToTheUserException
import es.uib.record.backend.groups.domain.exception.InvitationNotFoundException
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class RejectInvitationByIdUseCase(
    private val invitationRepository: InvitationRepository,
    private val userFacade: UserFacade,
) {
    fun execute(invitation: UUID, email: String) {
        val invitation =
            this.invitationRepository.findById(invitation)
                ?: throw InvitationNotFoundException(invitation)
        val userId = this.userFacade.getUserIdByEmail(email)

        if (invitation.inviteeUserId != userId) throw InvitationDoesNotBelongToTheUserException()

        this.invitationRepository.delete(invitation)
    }
}
