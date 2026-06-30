package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toAuthorDtos
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class GetPublicationDetailUseCase(
    private val publicationRepository: PublicationRepository,
    private val journalFacade: JournalFacade,
    private val userFacade: UserFacade,
    private val groupFacade: GroupFacade,
) {
    fun execute(publicationId: UUID, email: String): PublicationDetailDto {
        val publication =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        val userId = this.userFacade.getUserIdByEmail(email)
        if (!this.groupFacade.isMember(publication.groupId, userId)) {
            throw UserNotGroupMemberException(publication.groupId, userId)
        }

        // Privacidad: salvo que el usuario sea administrador, solo puede ver el detalle si alguno
        // de los autores de la publicación (creador o co-autores) no le oculta su historial. Si
        // todos se lo ocultan, se trata como inexistente para no revelar su existencia.
        if (!this.groupFacade.isAdmin(publication.groupId, userId)) {
            val hiddenOwners =
                this.groupFacade.getOwnersHiddenFromViewer(publication.groupId, userId)
            val authorIds =
                publication.authors.mapNotNull { it.internalUserId() } + publication.createdBy
            if (authorIds.none { it !in hiddenOwners }) {
                throw PublicationNotFoundException(publicationId)
            }
        }

        // Incluye el journal actual y todos los del historial (p. ej. el anterior a un reenvío).
        val journalIds =
            (publication.statusHistory.map { it.journalId } + publication.journalId).toSet()
        val journalNamesById =
            this.journalFacade.getJournalsByIds(journalIds).mapValues { it.value.name }
        val usersById =
            this.userFacade
                .getUsersByIds(publication.authors.mapNotNull { it.internalUserId() })
                .associateBy { it.userId }

        return publication.toDetailDto(
            journalNamesById,
            publication.authors.toAuthorDtos(usersById),
        )
    }
}
