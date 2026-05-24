package es.uib.record.backend.groups.application.usecase.group.dto

data class CreateGroupRequestDto(val name: String, val description: String? = null)
