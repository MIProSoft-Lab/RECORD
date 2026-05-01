package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.domain.TokenRepository
import org.springframework.stereotype.Component

@Component
class CheckTokenActiveUseCase(
    private val tokenRepository: TokenRepository,
) {
    fun execute(token: String): Boolean {
        val savedToken = this.tokenRepository.findByToken(token) ?: return false

        return !savedToken.expired && !savedToken.revoked
    }
}