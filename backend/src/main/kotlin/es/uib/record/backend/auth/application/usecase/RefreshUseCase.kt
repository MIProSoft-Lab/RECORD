package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.application.usecase.dto.AuthResponseDto
import es.uib.record.backend.auth.domain.exception.InvalidRefreshTokenException
import es.uib.record.backend.security.open.JwtService
import org.springframework.stereotype.Component

@Component
class RefreshUseCase(
    private val jwtService: JwtService,
) {
    fun execute(refreshToken: String): AuthResponseDto {
        val email = this.jwtService.extractEmail(refreshToken)

        if (email.isNullOrEmpty() || !jwtService.isTokenValid(refreshToken)) {
            throw InvalidRefreshTokenException()
        }

        val jwtToken = this.jwtService.generateToken(email)

        return AuthResponseDto(jwtToken, refreshToken)
    }
}