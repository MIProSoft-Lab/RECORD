package es.uib.record.backend.groups.infrastructure.mapper

import es.uib.record.backend.groups.application.usecase.dto.CreateGroupRequestDto
import es.uib.record.backend.groups.domain.Group
import es.uib.record.backend.model.CreateGroupRequest
import es.uib.record.backend.model.GroupResponse
import java.time.ZoneOffset

fun CreateGroupRequest.toDto() = CreateGroupRequestDto(
    name = this.name,
    description = this.description
)

fun Group.toResponse() = GroupResponse(
    id = this.id!!,
    name = this.name,
    description = this.description,
    createdBy = this.createdBy,
)