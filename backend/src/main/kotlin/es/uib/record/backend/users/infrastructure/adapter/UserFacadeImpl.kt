package es.uib.record.backend.users.infrastructure.adapter

import es.uib.record.backend.users.application.usecase.CreateUserUseCase
import es.uib.record.backend.users.application.usecase.GetAllUsersByIdsUseCase
import es.uib.record.backend.users.application.usecase.GetUserByEmailUseCase
import es.uib.record.backend.users.application.usecase.SearchUserUseCase
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import es.uib.record.backend.users.infrastructure.mapper.toOpenDto
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class UserFacadeImpl(
    private val createUserUseCase: CreateUserUseCase,
    private val getUserByEmailUseCase: GetUserByEmailUseCase,
    private val getAllUsersByIdsUseCase: GetAllUsersByIdsUseCase,
    private val searchUserUseCase: SearchUserUseCase,
) : UserFacade {

    override fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
    ): UUID {
        val createdUser =
            this.createUserUseCase.execute(
                User(email = email, firstName = firstName, lastName = lastName, password = password)
            )

        return createdUser.id!!
    }

    override fun getUserIdByEmail(email: String): UUID {
        val user = this.getUserByEmailUseCase.execute(email)
        return user.id!!
    }

    override fun getUserById(userId: UUID): UserOpenDto {
        return this.getAllUsersByIdsUseCase.execute(listOf(userId)).firstOrNull()?.toOpenDto()
            ?: throw UserNotFoundException(userId)
    }

    override fun getUsersByIds(userIds: List<UUID>): List<UserOpenDto> {
        return this.getAllUsersByIdsUseCase.execute(userIds).map { it.toOpenDto() }
    }

    override fun searchUsers(query: String): List<UserOpenDto> {
        return this.searchUserUseCase.execute(query).map { it.toOpenDto() }
    }
}
