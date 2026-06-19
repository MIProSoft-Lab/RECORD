package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class ListInterestJournalsUseCaseTest {

    companion object {
        private const val EMAIL = "user@uib.es"
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b1")
        private val CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c1")
    }

    @Mock private lateinit var journalRepository: JournalRepository
    @Mock private lateinit var userFacade: UserFacade

    private fun useCase() = ListInterestJournalsUseCase(journalRepository, userFacade)

    private fun samplePage(): PageResult<JournalSearchItem> {
        val item =
            JournalSearchItem(
                journal = Journal(id = JOURNAL_ID, clarivateId = "C1", name = "Nature"),
                year = 2023,
                categories = emptyList(),
                isInterest = true,
            )
        return PageResult(items = listOf(item), totalElements = 1, page = 0, size = 20)
    }

    @Test
    fun `resolves the user, trims the name and delegates the filters`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(
                journalRepository.searchInterests(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                )
            )
            .willReturn(samplePage())

        useCase().execute(EMAIL, "  Nature  ", CATEGORY_ID, Quartile.Q1, 0, 20)

        val nameCaptor = argumentCaptor<String>()
        verify(journalRepository)
            .searchInterests(eq(USER_ID), nameCaptor.capture(), any(), any(), any(), any())
        assertEquals("Nature", nameCaptor.firstValue)
    }

    @Test
    fun `returns the page produced by the repository`() {
        val page = samplePage()
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(
                journalRepository.searchInterests(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                )
            )
            .willReturn(page)

        assertSame(page, useCase().execute(EMAIL, null, null, null, 0, 20))
    }
}
