package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.users.application.usecase.dto.UserUpdateRequestDto
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import org.springframework.stereotype.Component

@Component
class UpdateCurrentUserByEmailUseCase(
    private val userRepository: UserRepository
) {
    fun execute(email: String, userUpdateRequestDto: UserUpdateRequestDto): User {
        val existingUser = userRepository.findByEmail(email) ?: throw UserNotFoundException(email)

        val updatedUser = existingUser.copy(
            firstName = userUpdateRequestDto.firstName,
            lastName = userUpdateRequestDto.lastName
        )

        return userRepository.save(updatedUser)
    }
}