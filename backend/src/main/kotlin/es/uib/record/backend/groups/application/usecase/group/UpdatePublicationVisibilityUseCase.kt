package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.CannotChangeOwnPublicationVisibilityException
import es.uib.record.backend.groups.domain.exception.CannotHidePublicationsFromAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Establece si [targetMemberId] puede ver el historial de publicaciones del usuario actual en el
 * grupo. `canSee = false` lo oculta; `canSee = true` restaura la visibilidad por defecto. No se
 * puede ocultar a un administrador ni cambiar la visibilidad respecto a uno mismo. El efecto es
 * inmediato.
 */
@Component
class UpdatePublicationVisibilityUseCase(
    private val groupRepository: GroupRepository,
    private val publicationVisibilityRepository: PublicationVisibilityRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID, targetMemberId: UUID, canSee: Boolean) {
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val userId = this.userFacade.getUserIdByEmail(email)

        if (!group.isMember(userId)) throw NotGroupMemberException(userId, groupId)

        if (targetMemberId == userId)
            throw CannotChangeOwnPublicationVisibilityException(userId, groupId)

        if (!group.isMember(targetMemberId)) throw NotGroupMemberException(targetMemberId, groupId)

        if (canSee) {
            this.publicationVisibilityRepository.unhide(
                groupId = groupId,
                ownerId = userId,
                viewerId = targetMemberId,
            )
        } else {
            if (group.getMemberRole(targetMemberId) == GroupRole.ADMIN)
                throw CannotHidePublicationsFromAdminException(targetMemberId, groupId)
            this.publicationVisibilityRepository.hide(
                groupId = groupId,
                ownerId = userId,
                viewerId = targetMemberId,
            )
        }
    }
}
