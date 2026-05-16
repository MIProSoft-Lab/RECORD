package es.uib.record.backend.groups.application.usecase

import es.uib.record.backend.groups.application.usecase.dto.CreateGroupRequestDto
import es.uib.record.backend.groups.domain.Group
import es.uib.record.backend.groups.domain.GroupRepository
import es.uib.record.backend.groups.domain.GroupRole
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component

@Component
class CreateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade
) {
    fun execute(createGroupRequestDto: CreateGroupRequestDto, email: String): Group {
        val userId = this.userFacade.getUserIdByEmail(email)

        val toSave = Group(
            name = createGroupRequestDto.name,
            description = createGroupRequestDto.description,
            createdBy = userId,
        )
        toSave.addMember(userId, GroupRole.ADMIN)

        return this.groupRepository.save(toSave)
    }
}