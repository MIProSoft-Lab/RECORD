package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Devuelve los miembros que ocultan su historial de publicaciones al usuario actual en el grupo
 * (para mostrarlos bloqueados en la pestaña de publicaciones). Los administradores siempre ven
 * todo, por lo que para un administrador el resultado es siempre vacío.
 */
@Component
class GetMembersHiddenFromMeUseCase(
    private val groupRepository: GroupRepository,
    private val publicationVisibilityRepository: PublicationVisibilityRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID): Set<UUID> {
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val userId = this.userFacade.getUserIdByEmail(email)

        if (!group.isMember(userId)) throw NotGroupMemberException(userId, groupId)

        if (group.getMemberRole(userId) == GroupRole.ADMIN) return emptySet()

        return this.publicationVisibilityRepository.findOwnersHiddenFromViewer(groupId, userId)
    }
}
