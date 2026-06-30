package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class SearchMyPublicationsUseCaseTest {

    companion object {
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks private lateinit var searchMyPublicationsUseCase: SearchMyPublicationsUseCase

    private fun publication() =
        Publication(
            id = UUID.randomUUID(),
            title = "First",
            journalId = JOURNAL_ID,
            groupId = UUID.randomUUID(),
            status = PublicationStatus.PLANNED,
            createdBy = USER_ID,
        )

    @Test
    fun `should return the page enriched with the journal name`() {
        // Given
        val publication = publication()
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(
                publicationRepository.searchByAuthor(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            )
            .willReturn(PageResult(listOf(publication), totalElements = 1, page = 0, size = 20))
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID)))
            .willReturn(mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null)))

        // When
        val result =
            searchMyPublicationsUseCase.execute(
                email = EMAIL,
                title = null,
                journalId = null,
                status = null,
                minDaysInStatus = null,
                onlyAsMainAuthor = false,
                page = 0,
                size = 20,
            )

        // Then
        assertEquals(1, result.totalElements)
        assertEquals(1, result.items.size)
        assertEquals(publication.id, result.items[0].id)
        assertEquals("Nature", result.items[0].journalName)
    }

    @Test
    fun `should not apply the stale filter when minDaysInStatus is null`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(
                publicationRepository.searchByAuthor(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            )
            .willReturn(PageResult(emptyList(), totalElements = 0, page = 0, size = 20))

        // When
        searchMyPublicationsUseCase.execute(
            email = EMAIL,
            title = "  ", // blanco → debe normalizarse a null
            journalId = null,
            status = null,
            minDaysInStatus = null,
            onlyAsMainAuthor = false,
            page = 0,
            size = 20,
        )

        // Then
        val staleBefore = argumentCaptor<Instant>()
        val excludeFinal = argumentCaptor<Boolean>()
        val title = argumentCaptor<String>()
        verify(publicationRepository)
            .searchByAuthor(
                eq(USER_ID),
                title.capture(),
                anyOrNull(),
                anyOrNull(),
                staleBefore.capture(),
                excludeFinal.capture(),
                any(),
                any(),
                any(),
            )
        assertNull(title.firstValue)
        assertNull(staleBefore.firstValue)
        assertFalse(excludeFinal.firstValue)
    }

    @Test
    fun `should apply the stale filter and exclude final statuses when minDaysInStatus is set`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(
                publicationRepository.searchByAuthor(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            )
            .willReturn(PageResult(emptyList(), totalElements = 0, page = 0, size = 20))
        val before = Instant.now().minus(Duration.ofDays(30))

        // When
        searchMyPublicationsUseCase.execute(
            email = EMAIL,
            title = null,
            journalId = null,
            status = null,
            minDaysInStatus = 30,
            onlyAsMainAuthor = false,
            page = 0,
            size = 20,
        )

        // Then
        val staleBefore = argumentCaptor<Instant>()
        val excludeFinal = argumentCaptor<Boolean>()
        verify(publicationRepository)
            .searchByAuthor(
                eq(USER_ID),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                staleBefore.capture(),
                excludeFinal.capture(),
                any(),
                any(),
                any(),
            )
        assertTrue(excludeFinal.firstValue)
        // El umbral debe rondar now - 30 días (tolerancia amplia para el tiempo de ejecución).
        val after = Instant.now().minus(Duration.ofDays(30))
        assertTrue(!staleBefore.firstValue.isBefore(before.minusSeconds(5)))
        assertTrue(!staleBefore.firstValue.isAfter(after.plusSeconds(5)))
    }
}
