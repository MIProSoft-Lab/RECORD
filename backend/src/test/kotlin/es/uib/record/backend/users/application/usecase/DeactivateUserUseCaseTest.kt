package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.auth.open.AuthFacade
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import es.uib.record.backend.users.domain.exception.UserDeactivatedException
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class DeactivateUserUseCaseTest {

    companion object {
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private const val EMAIL = "user@test.com"
        private const val CORRECT_PASSWORD = "correctPassword"
        private const val WRONG_PASSWORD = "wrongPassword"
        private const val ENCODED_PASSWORD = "\$2a\$10\$encodedHash"
    }

    @Mock private lateinit var userRepository: UserRepository

    @Mock private lateinit var passwordEncoder: PasswordEncoder

    @Mock private lateinit var authFacade: AuthFacade

    @InjectMocks private lateinit var deactivateUserUseCase: DeactivateUserUseCase

    @Test
    fun `should deactivate account and revoke tokens when user is active and password is correct`() {
        // Given
        val activeUser = createActiveUser()
        given(userRepository.findByEmail(EMAIL)).willReturn(activeUser)
        given(passwordEncoder.matches(CORRECT_PASSWORD, ENCODED_PASSWORD)).willReturn(true)
        given(userRepository.save(org.mockito.kotlin.any())).willAnswer { it.arguments[0] as User }

        // When
        deactivateUserUseCase.execute(EMAIL, CORRECT_PASSWORD)

        // Then
        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertNotNull(captor.firstValue.deactivatedAt)
        verify(authFacade).revokeAllTokensByEmail(EMAIL)
    }

    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Given
        given(userRepository.findByEmail(EMAIL)).willReturn(null)

        // When + Then
        assertThrows<UserNotFoundException> {
            deactivateUserUseCase.execute(EMAIL, CORRECT_PASSWORD)
        }
        verify(userRepository, never()).save(org.mockito.kotlin.any())
        verify(authFacade, never()).revokeAllTokensByEmail(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw UserDeactivatedException when account is already deactivated`() {
        // Given
        val deactivatedUser = createActiveUser().copy(deactivatedAt = Instant.now())
        given(userRepository.findByEmail(EMAIL)).willReturn(deactivatedUser)

        // When + Then
        assertThrows<UserDeactivatedException> {
            deactivateUserUseCase.execute(EMAIL, CORRECT_PASSWORD)
        }
        verify(userRepository, never()).save(org.mockito.kotlin.any())
        verify(authFacade, never()).revokeAllTokensByEmail(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw BadCredentialsException when password is wrong`() {
        // Given
        val activeUser = createActiveUser()
        given(userRepository.findByEmail(EMAIL)).willReturn(activeUser)
        given(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).willReturn(false)

        // When + Then
        assertThrows<BadCredentialsException> {
            deactivateUserUseCase.execute(EMAIL, WRONG_PASSWORD)
        }
        verify(userRepository, never()).save(org.mockito.kotlin.any())
        verify(authFacade, never()).revokeAllTokensByEmail(org.mockito.kotlin.any())
    }

    private fun createActiveUser() =
        User(
            id = USER_ID,
            email = EMAIL,
            password = ENCODED_PASSWORD,
            firstName = "Test",
            lastName = "User",
        )
}
