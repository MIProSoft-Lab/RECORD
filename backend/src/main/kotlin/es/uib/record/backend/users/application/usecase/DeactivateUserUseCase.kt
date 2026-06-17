package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.auth.open.AuthFacade
import es.uib.record.backend.users.domain.UserRepository
import es.uib.record.backend.users.domain.exception.UserDeactivatedException
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import java.time.Instant
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DeactivateUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authFacade: AuthFacade,
) {
    fun execute(email: String, password: String) {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException(email)

        if (user.isDeactivated()) {
            throw UserDeactivatedException()
        }

        if (!passwordEncoder.matches(password, user.password)) {
            throw BadCredentialsException("Invalid password")
        }

        val deactivatedUser = user.copy(deactivatedAt = Instant.now())
        userRepository.save(deactivatedUser)

        authFacade.revokeAllTokensByEmail(email)
    }
}
