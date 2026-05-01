package es.uib.record.backend.auth.infrastructure.adapter

import es.uib.record.backend.auth.application.usecase.CheckTokenActiveUseCase
import es.uib.record.backend.auth.open.AuthFacade
import org.springframework.stereotype.Component

@Component
class AuthFacadeImpl(
    private val checkTokenActiveUseCase: CheckTokenActiveUseCase
) : AuthFacade {
    override fun isTokenActive(token: String): Boolean {
        return this.checkTokenActiveUseCase.execute(token)
    }
}