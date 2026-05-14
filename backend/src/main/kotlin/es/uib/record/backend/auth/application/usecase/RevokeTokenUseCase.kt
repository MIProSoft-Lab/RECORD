package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.domain.Token
import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.auth.domain.exception.InvalidRefreshTokenException
import es.uib.record.backend.users.open.UserFacade
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class RevokeTokenUseCase(
    private val userFacade: UserFacade,
    private val tokenRepository: TokenRepository
) {
    fun execute(token: String) {
        val userEmail = SecurityContextHolder.getContext().authentication.name
        val userId = this.userFacade.getUserIdByEmail(userEmail)
        val savedToken = this.tokenRepository.findByToken(token)
            ?: throw InvalidRefreshTokenException()

        if (savedToken.userId != userId) {
            throw InvalidRefreshTokenException()
        }

        val revokedToken = Token(
            savedToken.id,
            savedToken.userId,
            savedToken.token,
            true,
        )

        this.tokenRepository.save(revokedToken)
    }
}