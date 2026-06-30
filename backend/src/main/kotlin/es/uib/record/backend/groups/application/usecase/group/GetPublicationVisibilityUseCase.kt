package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.application.usecase.group.dto.PublicationVisibilityMemberDto
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Devuelve, para cada otro miembro del grupo, si puede ver el historial de publicaciones del
 * usuario actual. Por defecto todos pueden verlo; los administradores siempre pueden y se devuelven
 * con `locked = true` (no se les puede ocultar).
 */
@Component
class GetPublicationVisibilityUseCase(
    private val groupRepository: GroupRepository,
    private val publicationVisibilityRepository: PublicationVisibilityRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID): List<PublicationVisibilityMemberDto> {
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val userId = this.userFacade.getUserIdByEmail(email)

        if (!group.isMember(userId)) throw NotGroupMemberException(userId, groupId)

        val hiddenViewers =
            this.publicationVisibilityRepository.findViewersHiddenByOwner(groupId, userId)
        val otherMemberIds = group.getMembersIds().filter { it != userId }
        val users = this.userFacade.getUsersByIds(otherMemberIds)

        return otherMemberIds.map { memberId ->
            val user = users.first { it.userId == memberId }
            val isAdmin = group.getMemberRole(memberId) == GroupRole.ADMIN
            PublicationVisibilityMemberDto(
                userId = memberId,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                role = group.getMemberRole(memberId),
                profileImageUrl = user.profileImageUrl,
                canSee = isAdmin || memberId !in hiddenViewers,
                locked = isAdmin,
            )
        }
    }
}
