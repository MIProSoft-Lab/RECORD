package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import es.uib.record.backend.users.domain.exception.EmailAlreadyInUseException
import org.springframework.stereotype.Component

@Component
class CreateUserUseCase(
    private val userRepository: UserRepository
) {
    fun execute(user: User): User {
        this.userRepository
            .findByEmail(user.email)
            ?.let { throw EmailAlreadyInUseException(user.email) }

        val toSave = user.copy(
            profileImageUrl = "https://api.dicebear.com/7.x/identicon/svg?seed=${user.email}"
        )

        return this.userRepository.save(toSave)
    }
}