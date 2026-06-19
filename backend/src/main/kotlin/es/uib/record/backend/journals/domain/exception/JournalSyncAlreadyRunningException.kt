package es.uib.record.backend.journals.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class JournalSyncAlreadyRunningException :
    DomainException(
        message = "A journal synchronization is already running",
        code = "errors.journal_sync_already_running",
        type = ErrorType.CONFLICT,
    )
