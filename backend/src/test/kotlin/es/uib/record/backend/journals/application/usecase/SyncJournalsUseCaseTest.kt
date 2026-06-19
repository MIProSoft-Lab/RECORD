package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.application.service.JournalSyncWriter
import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.journals.domain.model.SyncStatus
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import es.uib.record.backend.journals.infrastructure.client.ClarivateClient
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateBibliographicResponse
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateJournalsPageResponse
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateMetricsResponse
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class SyncJournalsUseCaseTest {

    companion object {
        private val SYNC_STATE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val RUN_STARTED_AT = Instant.parse("2026-06-18T00:00:00Z")
    }

    @Mock private lateinit var clarivateClient: ClarivateClient

    @Mock private lateinit var journalRepository: JournalRepository

    @Mock private lateinit var syncStateRepository: JournalSyncStateRepository

    @Mock private lateinit var journalSyncWriter: JournalSyncWriter

    private fun useCase() =
        SyncJournalsUseCase(
            clarivateClient,
            journalRepository,
            syncStateRepository,
            journalSyncWriter,
            yearsToKeep = 2,
        )

    private fun runningState() =
        SyncState(
            id = SYNC_STATE_ID,
            status = SyncStatus.RUNNING,
            runStartedAt = RUN_STARTED_AT,
            clarivateLastUpdated = "old",
        )

    private fun page(vararg ids: String) =
        ClarivateJournalsPageResponse(
            metadata = ClarivateJournalsPageResponse.Metadata(total = ids.size),
            hits = ids.map { ClarivateJournalsPageResponse.Hit(id = it, name = "Journal $it") },
        )

    private fun bibliographic(vararg years: Int) =
        ClarivateBibliographicResponse(
            name = "Some Journal",
            publisher = ClarivateBibliographicResponse.Publisher(name = "WILEY"),
            journalCitationReports =
                years.map { ClarivateBibliographicResponse.JournalCitationReport(it) },
        )

    private fun metrics(year: Int) = ClarivateMetricsResponse(year = year, suppressed = false)

    @Test
    fun `a failing journal does not abort the run and is counted as failed`() {
        given(syncStateRepository.get()).willReturn(runningState())
        given(clarivateClient.getLastUpdated()).willReturn("new")
        given(clarivateClient.getJournalsPage(eq(1), any())).willReturn(page("A", "B"))
        given(clarivateClient.getJournalsPage(eq(2), any())).willReturn(page())
        given(journalRepository.findByClarivateId(any())).willReturn(null)
        // A fails, B succeeds
        given(clarivateClient.getBibliographic("A")).willThrow(RuntimeException("boom"))
        given(clarivateClient.getBibliographic("B")).willReturn(bibliographic(2024))
        given(clarivateClient.getMetrics("B", 2024)).willReturn(metrics(2024))

        useCase().doSync()

        verify(journalSyncWriter).persist(eq("B"), any(), any(), any())
        verify(journalSyncWriter, never()).persist(eq("A"), any(), any(), any())

        val captor = argumentCaptor<SyncState>()
        verify(syncStateRepository, org.mockito.kotlin.atLeastOnce()).save(captor.capture())
        val finalState = captor.allValues.last()
        assertEquals(SyncStatus.SUCCESS, finalState.status)
        assertEquals(1, finalState.processedCount)
        assertEquals(1, finalState.failedCount)
        assertEquals("new", finalState.clarivateLastUpdated)
    }

    @Test
    fun `already synced journals in the current run are skipped`() {
        given(syncStateRepository.get()).willReturn(runningState())
        given(clarivateClient.getLastUpdated()).willReturn("new")
        given(clarivateClient.getJournalsPage(eq(1), any())).willReturn(page("A"))
        given(clarivateClient.getJournalsPage(eq(2), any())).willReturn(page())
        // A was already synced after the run started -> skip
        given(journalRepository.findByClarivateId("A"))
            .willReturn(
                Journal(
                    id = UUID.randomUUID(),
                    clarivateId = "A",
                    name = "A",
                    lastSyncedAt = RUN_STARTED_AT.plusSeconds(10),
                )
            )

        useCase().doSync()

        verify(clarivateClient, never()).getBibliographic(any())
        verify(journalSyncWriter, never()).persist(any(), any(), any(), any())
    }

    @Test
    fun `only the two most recent available years are fetched`() {
        given(syncStateRepository.get()).willReturn(runningState())
        given(clarivateClient.getLastUpdated()).willReturn("new")
        given(clarivateClient.getJournalsPage(eq(1), any())).willReturn(page("A"))
        given(clarivateClient.getJournalsPage(eq(2), any())).willReturn(page())
        given(journalRepository.findByClarivateId(any())).willReturn(null)
        given(clarivateClient.getBibliographic("A"))
            .willReturn(bibliographic(2020, 2025, 2024, 2023))
        given(clarivateClient.getMetrics("A", 2025)).willReturn(metrics(2025))
        given(clarivateClient.getMetrics("A", 2024)).willReturn(metrics(2024))

        useCase().doSync()

        verify(clarivateClient).getMetrics("A", 2025)
        verify(clarivateClient).getMetrics("A", 2024)
        verify(clarivateClient, never()).getMetrics("A", 2023)
        verify(clarivateClient, never()).getMetrics("A", 2020)

        val captor = argumentCaptor<List<ClarivateMetricsResponse>>()
        verify(journalSyncWriter).persist(eq("A"), any(), any(), captor.capture())
        assertEquals(listOf(2025, 2024), captor.firstValue.map { it.year })
    }
}
