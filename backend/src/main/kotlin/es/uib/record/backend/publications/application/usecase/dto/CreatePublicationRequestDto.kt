package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.PublicationStatus
import java.util.UUID

data class CreatePublicationRequestDto(
    val title: String,
    val abstractText: String?,
    val doi: String?,
    val journalId: UUID,
    val groupId: UUID,
    val status: PublicationStatus?,
)
