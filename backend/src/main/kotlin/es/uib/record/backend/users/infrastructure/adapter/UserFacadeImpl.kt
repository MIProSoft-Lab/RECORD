package es.uib.record.backend.users.infrastructure.adapter

import es.uib.record.backend.users.application.usecase.CreateUserUseCase
import es.uib.record.backend.users.application.usecase.GetAllUsersByIdsUseCase
import es.uib.record.backend.users.application.usecase.GetUserByEmailUseCase
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.infrastructure.mapper.toOpenDto
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserFacadeImpl(
    private val createUserUseCase: CreateUserUseCase,
    private val getUserByEmailUseCase: GetUserByEmailUseCase,
    private val getAllUsersByIdsUseCase: GetAllUsersByIdsUseCase
) : UserFacade {

    override fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): UUID {
        val createdUser = this.createUserUseCase.execute(User(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password
        ))

        return createdUser.id!!
    }

    override fun getUserIdByEmail(email: String): UUID {
        val user = this.getUserByEmailUseCase.execute(email)
        return user.id!!
    }

    override fun getUsersByIds(userIds: List<UUID>): List<UserOpenDto> {
        return this.getAllUsersByIdsUseCase.execute(userIds).map(User::toOpenDto)
    }
}