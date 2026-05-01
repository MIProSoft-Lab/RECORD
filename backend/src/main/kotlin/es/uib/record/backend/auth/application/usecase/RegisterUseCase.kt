package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.application.usecase.dto.AuthResponseDto
import es.uib.record.backend.auth.application.usecase.dto.RegisterRequestDto
import es.uib.record.backend.auth.domain.Token
import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.auth.infrastructure.persistence.TokenEntity
import es.uib.record.backend.security.open.JwtService
import es.uib.record.backend.users.open.UserFacade
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RegisterUseCase(
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val userFacade: UserFacade,
    private val tokenRepository: TokenRepository
) {
    fun execute(registerRequestDto: RegisterRequestDto): AuthResponseDto {
        val userId = this.userFacade.createUser(
            registerRequestDto.email,
            registerRequestDto.firstName,
            registerRequestDto.lastName,
            this.passwordEncoder.encode(registerRequestDto.password)
        )

        val jwtToken = this.jwtService.generateToken(registerRequestDto.email)
        val refreshToken = this.jwtService.generateRefreshToken(registerRequestDto.email)

        this.saveToken(jwtToken, userId)

        return AuthResponseDto(jwtToken, refreshToken)
    }

    private fun saveToken(jwt: String, userId: UUID) {
        val token = Token(userId = userId, token = jwt)
        tokenRepository.save(token)
    }
}