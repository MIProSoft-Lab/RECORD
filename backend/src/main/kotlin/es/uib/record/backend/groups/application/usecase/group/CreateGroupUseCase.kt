package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.application.usecase.group.dto.CreateGroupRequestDto
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component

@Component
class CreateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade,
) {
    fun execute(createGroupRequestDto: CreateGroupRequestDto, email: String): Group {
        val userId = this.userFacade.getUserIdByEmail(email)

        val toSave =
            Group(
                name = createGroupRequestDto.name,
                description = createGroupRequestDto.description,
                createdBy = userId,
            )
        toSave.addMember(userId, GroupRole.ADMIN)

        return this.groupRepository.save(toSave)
    }
}
