package es.uib.record.backend.auth.application.usecase.dto

data class AuthResponseDto(
    val token: String,
    val refreshToken: String
)