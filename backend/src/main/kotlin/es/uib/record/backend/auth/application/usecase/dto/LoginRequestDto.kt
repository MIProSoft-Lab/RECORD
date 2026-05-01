package es.uib.record.backend.auth.application.usecase.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)