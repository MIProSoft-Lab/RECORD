package es.uib.record.backend.journals.application.scheduler

import es.uib.record.backend.journals.application.usecase.CheckClarivateUpdateUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class JournalSyncScheduler(private val checkClarivateUpdateUseCase: CheckClarivateUpdateUseCase) {

    @Scheduled(cron = $$"${application.clarivate.sync.cron}")
    fun checkForUpdates() {
        checkClarivateUpdateUseCase.execute()
    }
}
