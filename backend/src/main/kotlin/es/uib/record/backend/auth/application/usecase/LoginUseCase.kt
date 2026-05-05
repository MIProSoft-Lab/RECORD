package es.uib.record.backend.auth.application.usecase

import es.uib.record.backend.auth.application.usecase.dto.AuthResponseDto
import es.uib.record.backend.auth.application.usecase.dto.LoginRequestDto
import es.uib.record.backend.auth.domain.Token
import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.security.open.JwtService
import es.uib.record.backend.users.open.UserFacade
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LoginUseCase(
    private val authenticationManager: AuthenticationManager,
    private val userFacade: UserFacade,
    private val jwtService: JwtService,
    private val tokenRepository: TokenRepository
) {
    fun execute(loginRequestDto: LoginRequestDto): AuthResponseDto {
        this.authenticationManager.authenticate(
           UsernamePasswordAuthenticationToken(
               loginRequestDto.email,
               loginRequestDto.password
           )
        )

        val userId = this.userFacade.getUserIdByEmail(loginRequestDto.email)

        val jwtToken = this.jwtService.generateToken(loginRequestDto.email)
        val refreshToken = this.jwtService.generateRefreshToken(loginRequestDto.email)

        this.saveToken(refreshToken, userId)

        return AuthResponseDto(jwtToken, refreshToken)
    }

    private fun saveToken(jwt: String, userId: UUID) {
        val token = Token(userId = userId, token = jwt)
        this.tokenRepository.save(token)
    }
}