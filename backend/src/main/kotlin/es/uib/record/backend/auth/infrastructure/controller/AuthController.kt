package es.uib.record.backend.auth.infrastructure.controller

import es.uib.record.backend.api.AuthApi
import es.uib.record.backend.auth.application.usecase.LoginUseCase
import es.uib.record.backend.auth.application.usecase.RefreshUseCase
import es.uib.record.backend.auth.application.usecase.RegisterUseCase
import es.uib.record.backend.auth.infrastructure.mapper.toDto
import es.uib.record.backend.auth.infrastructure.mapper.toResponse
import es.uib.record.backend.model.AuthResponse
import es.uib.record.backend.model.LoginRequest
import es.uib.record.backend.model.RefreshRequest
import es.uib.record.backend.model.RegisterRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshUseCase
) : AuthApi {

    override fun login(loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val authResponse: AuthResponse = loginUseCase.execute(loginRequest.toDto()).toResponse()
        return ResponseEntity.ok(authResponse)
    }

    override fun register(registerRequest: RegisterRequest): ResponseEntity<AuthResponse> {
        val authResponse: AuthResponse = registerUseCase.execute(registerRequest.toDto()).toResponse()
        return ResponseEntity.ok(authResponse)
    }

    override fun refresh(refreshRequest: RefreshRequest): ResponseEntity<AuthResponse> {
        val authResponse: AuthResponse = refreshUseCase.execute(refreshRequest.token).toResponse()
        return ResponseEntity.ok(authResponse)
    }
}