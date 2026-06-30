package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.PublicationStatus
import java.time.Instant
import java.util.UUID

/** Creador (dueño) de una publicación, para mostrar de quién es cada fila en el listado de grupo. */
data class PublicationCreatorDto(
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val profileImageUrl: String?,
)

/** Resumen de una publicación dentro del listado de un grupo, enriquecido con su creador. */
data class GroupPublicationSummaryDto(
    val id: UUID,
    val title: String,
    val journalId: UUID,
    val journalName: String?,
    val groupId: UUID,
    val status: PublicationStatus,
    val createdAt: Instant,
    val statusChangedAt: Instant,
    val creator: PublicationCreatorDto,
)
