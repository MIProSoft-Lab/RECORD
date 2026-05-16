package es.uib.record.backend.groups.application.usecase.dto

data class CreateGroupRequestDto(
    val name: String,
    val description: String? = null
)