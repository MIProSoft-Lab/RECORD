package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.application.usecase.dto.AuthResponseDto
import es.uib.record.backend.auth.domain.Token
import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.auth.domain.exception.InvalidRefreshTokenException
import es.uib.record.backend.security.open.JwtService
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RefreshUseCase(
    private val jwtService: JwtService,
    private val userFacade: UserFacade,
    private val tokenRepository: TokenRepository
) {
    fun execute(refreshToken: String): AuthResponseDto {
        val email = this.jwtService.extractEmail(refreshToken)

        val userId = this.userFacade.getUserIdByEmail(email)

        if (!jwtService.isTokenValid(refreshToken)) {
            throw InvalidRefreshTokenException()
        }

        val jwtToken = this.jwtService.generateToken(email)
        this.saveToken(jwtToken, userId)

        return AuthResponseDto(jwtToken, refreshToken)
    }

    private fun saveToken(jwt: String, userId: UUID) {
        val token = Token(userId = userId, token = jwt)
        tokenRepository.save(token)
    }
}