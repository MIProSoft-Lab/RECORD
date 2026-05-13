package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import org.springframework.stereotype.Component

@Component
class UpdateUserPushNotificationsUseCase(
    private val userRepository: UserRepository
) {
    fun execute(email: String, pushNotificationsEnabled: Boolean): User {
        val existingUser = userRepository.findByEmail(email) ?: throw UserNotFoundException(email)

        val updatedUser = existingUser.copy(
            pushNotifications = pushNotificationsEnabled
        )

        return userRepository.save(updatedUser)
    }
}