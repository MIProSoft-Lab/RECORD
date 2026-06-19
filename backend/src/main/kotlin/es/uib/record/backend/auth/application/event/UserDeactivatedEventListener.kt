package es.uib.record.backend.auth.application.event

import es.uib.record.backend.auth.application.usecase.RevokeAllTokensByEmailUseCase
import es.uib.record.backend.users.open.UserDeactivatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserDeactivatedEventListener(
    private val revokeAllTokensByEmailUseCase: RevokeAllTokensByEmailUseCase
) {
    @EventListener
    fun onUserDeactivated(event: UserDeactivatedEvent) {
        revokeAllTokensByEmailUseCase.execute(event.email)
    }
}
