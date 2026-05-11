package es.uib.record.backend.users.application.usecase.dto

data class UserProfileImageSignatureResponseDto(
    val signature: String,
    val timestamp: String,
    val apiKey: String,
    val cloudName: String,
    val transformation: String,
    val folder: String
)