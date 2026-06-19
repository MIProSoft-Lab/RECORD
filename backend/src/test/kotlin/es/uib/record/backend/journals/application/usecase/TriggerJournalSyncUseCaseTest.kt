package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.InvalidSyncTokenException
import es.uib.record.backend.journals.domain.exception.JournalSyncAlreadyRunningException
import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.journals.domain.model.SyncStatus
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class TriggerJournalSyncUseCaseTest {

    companion object {
        private const val TOKEN = "secret-token"
        private val SYNC_STATE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
    }

    @Mock private lateinit var syncStateRepository: JournalSyncStateRepository

    @Mock private lateinit var syncJournalsUseCase: SyncJournalsUseCase

    private fun useCase() =
        TriggerJournalSyncUseCase(syncStateRepository, syncJournalsUseCase, triggerToken = TOKEN)

    @Test
    fun `throws when the provided token is wrong`() {
        assertThrows<InvalidSyncTokenException> { useCase().execute("wrong") }
        verify(syncJournalsUseCase, never()).tryStart()
    }

    @Test
    fun `throws when the token is missing`() {
        assertThrows<InvalidSyncTokenException> { useCase().execute(null) }
        verify(syncJournalsUseCase, never()).tryStart()
    }

    @Test
    fun `throws conflict when a sync is already running`() {
        given(syncJournalsUseCase.tryStart()).willReturn(false)

        assertThrows<JournalSyncAlreadyRunningException> { useCase().execute(TOKEN) }
        verify(syncJournalsUseCase, never()).runAsync()
    }

    @Test
    fun `launches the sync and returns the current state on success`() {
        val state = SyncState(id = SYNC_STATE_ID, status = SyncStatus.RUNNING)
        given(syncJournalsUseCase.tryStart()).willReturn(true)
        given(syncStateRepository.get()).willReturn(state)

        val result = useCase().execute(TOKEN)

        assertEquals(SyncStatus.RUNNING, result.status)
        verify(syncJournalsUseCase).runAsync()
    }
}
