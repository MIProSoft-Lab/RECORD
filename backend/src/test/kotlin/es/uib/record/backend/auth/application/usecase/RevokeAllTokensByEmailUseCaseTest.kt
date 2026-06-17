package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class RevokeAllTokensByEmailUseCaseTest {

    companion object {
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var tokenRepository: TokenRepository

    @InjectMocks private lateinit var revokeAllTokensByEmailUseCase: RevokeAllTokensByEmailUseCase

    @Test
    fun `should resolve email to userId and revoke all tokens`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)

        // When
        revokeAllTokensByEmailUseCase.execute(EMAIL)

        // Then
        verify(userFacade).getUserIdByEmail(EMAIL)
        verify(tokenRepository).revokeAllByUserId(USER_ID)
    }
}
