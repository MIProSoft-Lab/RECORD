package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.journals.domain.model.SyncStatus
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import es.uib.record.backend.journals.infrastructure.client.ClarivateClient
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CheckClarivateUpdateUseCaseTest {

    companion object {
        private val SYNC_STATE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
    }

    @Mock private lateinit var clarivateClient: ClarivateClient

    @Mock private lateinit var syncStateRepository: JournalSyncStateRepository

    @Mock private lateinit var syncJournalsUseCase: SyncJournalsUseCase

    @InjectMocks private lateinit var checkClarivateUpdateUseCase: CheckClarivateUpdateUseCase

    private fun state(status: SyncStatus, lastUpdated: String?) =
        SyncState(id = SYNC_STATE_ID, status = status, clarivateLastUpdated = lastUpdated)

    @Test
    fun `triggers sync when clarivate data changed`() {
        given(syncStateRepository.get()).willReturn(state(SyncStatus.IDLE, "v1"))
        given(clarivateClient.getLastUpdated()).willReturn("v2")
        given(syncJournalsUseCase.tryStart()).willReturn(true)

        checkClarivateUpdateUseCase.execute()

        verify(syncJournalsUseCase).tryStart()
        verify(syncJournalsUseCase).runAsync()
    }

    @Test
    fun `does nothing when clarivate data is unchanged`() {
        given(syncStateRepository.get()).willReturn(state(SyncStatus.SUCCESS, "v1"))
        given(clarivateClient.getLastUpdated()).willReturn("v1")

        checkClarivateUpdateUseCase.execute()

        verify(syncJournalsUseCase, never()).tryStart()
        verify(syncJournalsUseCase, never()).runAsync()
    }

    @Test
    fun `does nothing when a sync is already running`() {
        given(syncStateRepository.get()).willReturn(state(SyncStatus.RUNNING, "v1"))

        checkClarivateUpdateUseCase.execute()

        verify(clarivateClient, never()).getLastUpdated()
        verify(syncJournalsUseCase, never()).tryStart()
    }

    @Test
    fun `resumes a previously failed run regardless of last-updated`() {
        given(syncStateRepository.get()).willReturn(state(SyncStatus.FAILED, "v1"))
        given(syncJournalsUseCase.tryStart()).willReturn(true)

        checkClarivateUpdateUseCase.execute()

        verify(clarivateClient, never()).getLastUpdated()
        verify(syncJournalsUseCase).tryStart()
        verify(syncJournalsUseCase).runAsync()
    }

    @Test
    fun `does not trigger when last-updated cannot be fetched`() {
        given(syncStateRepository.get()).willReturn(state(SyncStatus.IDLE, "v1"))
        given(clarivateClient.getLastUpdated()).willReturn(null)

        checkClarivateUpdateUseCase.execute()

        verify(syncJournalsUseCase, never()).tryStart()
    }
}
