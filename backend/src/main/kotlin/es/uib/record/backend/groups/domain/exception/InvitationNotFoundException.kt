package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class InvitationNotFoundException(invitationId: UUID) :
    DomainException(
        message = "Invitation with id $invitationId not found",
        code = "INVITATION_NOT_FOUND",
        type = ErrorType.NOT_FOUND,
        params = mapOf("id" to invitationId.toString()),
    )
