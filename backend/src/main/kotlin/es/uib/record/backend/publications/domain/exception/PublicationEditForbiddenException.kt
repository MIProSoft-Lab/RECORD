package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class PublicationEditForbiddenException(publicationId: UUID, userId: UUID) :
    DomainException(
        message = "User $userId is not allowed to edit publication $publicationId",
        code = "PUBLICATION_EDIT_FORBIDDEN",
        type = ErrorType.FORBIDDEN,
        params =
            mapOf(
                "publicationId" to publicationId.toString(),
                "userId" to userId.toString(),
            ),
    )
