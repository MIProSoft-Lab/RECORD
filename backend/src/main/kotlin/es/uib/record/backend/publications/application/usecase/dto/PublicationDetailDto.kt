package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.PublicationStatus
import java.time.Instant
import java.util.UUID

data class PublicationDetailDto(
    val id: UUID,
    val title: String,
    val abstractText: String?,
    val doi: String?,
    val journalId: UUID,
    val journalName: String?,
    val groupId: UUID,
    val status: PublicationStatus,
    val createdBy: UUID,
    val createdAt: Instant,
)
