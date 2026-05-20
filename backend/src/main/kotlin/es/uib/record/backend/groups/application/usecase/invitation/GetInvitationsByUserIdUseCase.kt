package es.uib.record.backend.groups.application.usecase.invitation

import es.uib.record.backend.groups.application.usecase.invitation.dto.GroupInvitationSummaryDto
import es.uib.record.backend.groups.application.usecase.invitation.dto.InvitationResponseDto
import es.uib.record.backend.groups.application.usecase.invitation.dto.InviterSummaryDto
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.model.Invitation
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component

@Component
class GetInvitationsByUserIdUseCase(
    private val invitationRepository: InvitationRepository,
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade
) {
    fun execute(email: String): List<InvitationResponseDto> {
        val userId = this.userFacade.getUserIdByEmail(email)
        val invitations = this.invitationRepository.findByInviteeUserId(userId)

        return invitations.map(this::toResponseDto)
    }

    fun toResponseDto(invitation: Invitation): InvitationResponseDto {
        val group = this.groupRepository.findById(invitation.groupId)
            ?: throw GroupNotFoundException(invitation.groupId)
        val user = this.userFacade.getUsersByIds(listOf(invitation.inviterUserId)).first()

        return InvitationResponseDto(
            id = invitation.id!!,
            group = GroupInvitationSummaryDto(
                group.id!!,
                group.name
            ),
            inviter = InviterSummaryDto(
                user.userId,
                user.firstName,
                user.lastName,
                user.profileImageUrl!!
            ),
            createdAt = invitation.createdAt
        )
    }
}