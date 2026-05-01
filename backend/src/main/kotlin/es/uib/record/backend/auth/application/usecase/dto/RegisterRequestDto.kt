package es.uib.record.backend.auth.application.usecase.dto

data class RegisterRequestDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)
