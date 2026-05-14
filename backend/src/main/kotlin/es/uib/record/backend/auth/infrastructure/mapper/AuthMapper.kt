package es.uib.record.backend.auth.infrastructure.mapper

import es.uib.record.backend.auth.application.usecase.dto.AuthResponseDto
import es.uib.record.backend.auth.application.usecase.dto.LoginRequestDto
import es.uib.record.backend.auth.application.usecase.dto.RegisterRequestDto
import es.uib.record.backend.auth.domain.Token
import es.uib.record.backend.auth.infrastructure.persistence.TokenEntity
import es.uib.record.backend.model.AuthResponse
import es.uib.record.backend.model.LoginRequest
import es.uib.record.backend.model.RegisterRequest

fun RegisterRequest.toDto() = RegisterRequestDto(
    this.email,
    this.firstName,
    this.lastName,
    this.password
)

fun LoginRequest.toDto() = LoginRequestDto(
    this.email,
    this.password
)

fun AuthResponseDto.toResponse() = AuthResponse(
    this.token,
    this.refreshToken
)

fun Token.toEntity() = TokenEntity(
    this.id,
    this.userId,
    this.token,
    this.revoked
)

fun TokenEntity.toDomain() = Token(
    this.id,
    this.userId,
    this.token,
    this.revoked
)