package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationAuthor
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID

/**
 * Construye los DTOs de autor preservando el orden: los internos se enriquecen con la información
 * del usuario en [usersById]; los externos usan sus nombres guardados.
 */
fun List<PublicationAuthor>.toAuthorDtos(
    usersById: Map<UUID, UserOpenDto>
): List<PublicationAuthorDto> =
    this.map { author ->
        when (author) {
            is PublicationAuthor.InternalAuthor -> {
                val user = usersById[author.userId]
                PublicationAuthorDto(
                    authorId = author.id!!,
                    type = PublicationAuthorType.INTERNAL,
                    userId = author.userId,
                    firstName = user?.firstName ?: "",
                    lastName = user?.lastName ?: "",
                    email = user?.email,
                    profileImageUrl = user?.profileImageUrl,
                )
            }
            is PublicationAuthor.ExternalAuthor ->
                PublicationAuthorDto(
                    authorId = author.id!!,
                    type = PublicationAuthorType.EXTERNAL,
                    userId = null,
                    firstName = author.firstName,
                    lastName = author.lastName,
                    email = null,
                    profileImageUrl = null,
                )
        }
    }

fun Publication.toDetailDto(
    journalNamesById: Map<UUID, String?>,
    authors: List<PublicationAuthorDto> = emptyList(),
) =
    PublicationDetailDto(
        id = this.id!!,
        title = this.title,
        abstractText = this.abstractText,
        doi = this.doi,
        journalId = this.journalId,
        journalName = journalNamesById[this.journalId],
        groupId = this.groupId,
        status = this.status,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
        authors = authors,
        statusHistory =
            this.statusHistory.map { entry ->
                PublicationStatusHistoryDto(
                    status = entry.status,
                    journalId = entry.journalId,
                    journalName = journalNamesById[entry.journalId],
                    changedAt = entry.changedAt,
                    comment = entry.comment,
                )
            },
    )

fun Publication.toSummaryDto(journalName: String?) =
    PublicationSummaryDto(
        id = this.id!!,
        title = this.title,
        journalId = this.journalId,
        journalName = journalName,
        groupId = this.groupId,
        status = this.status,
        createdAt = this.createdAt,
        // Fecha de entrada al estado actual: última transición registrada (fallback a la creación).
        statusChangedAt = this.statusHistory.lastOrNull()?.changedAt ?: this.createdAt,
    )

/**
 * Variante del resumen para el listado de un grupo: añade los datos del creador (dueño). [creator]
 * puede ser `null` si el usuario ya no es resoluble; en ese caso se rellena con nombre vacío.
 */
fun Publication.toGroupSummaryDto(journalName: String?, creator: UserOpenDto?) =
    GroupPublicationSummaryDto(
        id = this.id!!,
        title = this.title,
        journalId = this.journalId,
        journalName = journalName,
        groupId = this.groupId,
        status = this.status,
        createdAt = this.createdAt,
        statusChangedAt = this.statusHistory.lastOrNull()?.changedAt ?: this.createdAt,
        creator =
            PublicationCreatorDto(
                userId = this.createdBy,
                firstName = creator?.firstName ?: "",
                lastName = creator?.lastName ?: "",
                profileImageUrl = creator?.profileImageUrl,
            ),
    )
