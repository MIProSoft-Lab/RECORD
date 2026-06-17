package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component

@Component
class RevokeAllTokensByEmailUseCase(
    private val userFacade: UserFacade,
    private val tokenRepository: TokenRepository,
) {
    fun execute(email: String) {
        val userId = userFacade.getUserIdByEmail(email)
        tokenRepository.revokeAllByUserId(userId)
    }
}
