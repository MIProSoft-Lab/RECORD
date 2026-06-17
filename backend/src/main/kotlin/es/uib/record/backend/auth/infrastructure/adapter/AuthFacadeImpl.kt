package es.uib.record.backend.auth.infrastructure.adapter

import es.uib.record.backend.auth.application.usecase.CheckTokenActiveUseCase
import es.uib.record.backend.auth.application.usecase.RevokeAllTokensByEmailUseCase
import es.uib.record.backend.auth.open.AuthFacade
import org.springframework.stereotype.Component

@Component
class AuthFacadeImpl(
    private val checkTokenActiveUseCase: CheckTokenActiveUseCase,
    private val revokeAllTokensByEmailUseCase: RevokeAllTokensByEmailUseCase,
) : AuthFacade {
    override fun isTokenActive(token: String): Boolean {
        return this.checkTokenActiveUseCase.execute(token)
    }

    override fun revokeAllTokensByEmail(email: String) {
        this.revokeAllTokensByEmailUseCase.execute(email)
    }
}
