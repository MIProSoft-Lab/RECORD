package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class InvitationDoesNotBelongToTheUserException :
    DomainException(
        message = "Invitation does not belong to the user",
        code = "INVITATION_DOES_NOT_BELONG_TO_USER",
        type = ErrorType.FORBIDDEN,
    )
