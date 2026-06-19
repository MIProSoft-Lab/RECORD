package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalCategoryQuartileInfo
import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class SearchJournalsUseCaseTest {

    companion object {
        private const val EMAIL = "user@uib.es"
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b1")
        private val CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c1")
    }

    @Mock private lateinit var journalRepository: JournalRepository
    @Mock private lateinit var userJournalInterestRepository: UserJournalInterestRepository
    @Mock private lateinit var userFacade: UserFacade

    private fun useCase() =
        SearchJournalsUseCase(journalRepository, userJournalInterestRepository, userFacade)

    private fun samplePage(): PageResult<JournalSearchItem> {
        val item =
            JournalSearchItem(
                journal = Journal(id = JOURNAL_ID, clarivateId = "C1", name = "Nature"),
                year = 2023,
                categories =
                    listOf(
                        JournalCategoryQuartileInfo(
                            categoryId = CATEGORY_ID,
                            categoryName = "ONCOLOGY",
                            edition = "SCIE",
                            year = 2023,
                            quartile = Quartile.Q1,
                            impactFactor = BigDecimal("12.345"),
                        )
                    ),
            )
        return PageResult(items = listOf(item), totalElements = 1, page = 0, size = 20)
    }

    @Test
    fun `trims the name and delegates the filters to the repository`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(userJournalInterestRepository.findInterestJournalIds(USER_ID))
            .willReturn(emptySet())
        given(journalRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .willReturn(samplePage())

        useCase().execute(EMAIL, "  Nature  ", CATEGORY_ID, Quartile.Q1, 0, 20)

        val nameCaptor = argumentCaptor<String>()
        verify(journalRepository).search(nameCaptor.capture(), any(), any(), any(), any())
        assertEquals("Nature", nameCaptor.firstValue)
    }

    @Test
    fun `normalizes a blank name to null`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(userJournalInterestRepository.findInterestJournalIds(USER_ID))
            .willReturn(emptySet())
        given(journalRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .willReturn(samplePage())

        useCase().execute(EMAIL, "   ", null, null, 0, 20)

        verify(journalRepository).search(isNull(), anyOrNull(), anyOrNull(), any(), any())
    }

    @Test
    fun `browses with no filters and propagates pagination metadata`() {
        val page =
            PageResult<JournalSearchItem>(
                items = emptyList(),
                totalElements = 137,
                page = 2,
                size = 20,
            )
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(userJournalInterestRepository.findInterestJournalIds(USER_ID))
            .willReturn(emptySet())
        given(journalRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .willReturn(page)

        val result = useCase().execute(EMAIL, null, null, null, 2, 20)

        assertEquals(137, result.totalElements)
        assertEquals(2, result.page)
        assertEquals(7, result.totalPages)
    }

    @Test
    fun `marks isInterest for journals in the user's interest set`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(userJournalInterestRepository.findInterestJournalIds(USER_ID))
            .willReturn(setOf(JOURNAL_ID))
        given(journalRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .willReturn(samplePage())

        val result = useCase().execute(EMAIL, null, null, null, 0, 20)

        assertTrue(result.items.single().isInterest)
    }

    @Test
    fun `leaves isInterest false for journals outside the user's interest set`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(userJournalInterestRepository.findInterestJournalIds(USER_ID))
            .willReturn(emptySet())
        given(journalRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .willReturn(samplePage())

        val result = useCase().execute(EMAIL, null, null, null, 0, 20)

        assertFalse(result.items.single().isInterest)
    }
}
