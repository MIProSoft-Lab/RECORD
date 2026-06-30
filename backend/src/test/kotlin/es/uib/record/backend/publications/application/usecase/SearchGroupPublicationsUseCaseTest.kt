package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class SearchGroupPublicationsUseCaseTest {

    companion object {
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val OTHER_MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @Mock private lateinit var groupFacade: GroupFacade

    @InjectMocks private lateinit var searchGroupPublicationsUseCase: SearchGroupPublicationsUseCase

    private fun publication(createdBy: UUID = USER_ID) =
        Publication(
            id = UUID.randomUUID(),
            title = "First",
            journalId = JOURNAL_ID,
            groupId = GROUP_ID,
            status = PublicationStatus.PLANNED,
            createdBy = createdBy,
        )

    @Test
    fun `should throw when the user is not a member of the group`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(false)

        // When / Then
        assertThrows(UserNotGroupMemberException::class.java) {
            searchGroupPublicationsUseCase.execute(
                email = EMAIL,
                groupId = GROUP_ID,
                memberIds = null,
                title = null,
                journalId = null,
                status = null,
                minDaysInStatus = null,
                page = 0,
                size = 20,
            )
        }
        verify(publicationRepository, never())
            .searchByGroup(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), any())
    }

    @Test
    fun `should include all group publications enriched with journal name and creator when no member filter`() {
        // Given
        val publication = publication()
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
        given(
                publicationRepository.searchByGroup(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                )
            )
            .willReturn(PageResult(listOf(publication), totalElements = 1, page = 0, size = 20))
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID)))
            .willReturn(mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null)))
        given(userFacade.getUsersByIds(listOf(USER_ID)))
            .willReturn(listOf(UserOpenDto(USER_ID, "Ada", "Lovelace", "ada@test.com", "img")))

        // When
        val result =
            searchGroupPublicationsUseCase.execute(
                email = EMAIL,
                groupId = GROUP_ID,
                memberIds = null,
                title = null,
                journalId = null,
                status = null,
                minDaysInStatus = null,
                page = 0,
                size = 20,
            )

        // Then: authorIds null forwarded (all members), enriched response
        val authorIds = argumentCaptor<List<UUID>>()
        verify(publicationRepository)
            .searchByGroup(
                eq(GROUP_ID),
                authorIds.capture(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any(),
                any(),
            )
        assertNull(authorIds.firstValue)
        assertEquals(1, result.items.size)
        assertEquals("Nature", result.items[0].journalName)
        assertEquals("Ada", result.items[0].creator.firstName)
        assertEquals(USER_ID, result.items[0].creator.userId)
    }

    @Test
    fun `should intersect requested member ids with actual group members`() {
        // Given a requested id that is NOT a group member → it must be filtered out
        val foreignId = UUID.fromString("00000000-0000-0000-0000-0000000000ff")
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
        given(groupFacade.getMemberIds(GROUP_ID)).willReturn(listOf(USER_ID, OTHER_MEMBER_ID))
        given(
                publicationRepository.searchByGroup(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                )
            )
            .willReturn(PageResult(emptyList(), totalElements = 0, page = 0, size = 20))

        // When
        searchGroupPublicationsUseCase.execute(
            email = EMAIL,
            groupId = GROUP_ID,
            memberIds = listOf(OTHER_MEMBER_ID, foreignId),
            title = null,
            journalId = null,
            status = null,
            minDaysInStatus = null,
            page = 0,
            size = 20,
        )

        // Then only the actual member survives the intersection
        val authorIds = argumentCaptor<List<UUID>>()
        verify(publicationRepository)
            .searchByGroup(
                eq(GROUP_ID),
                authorIds.capture(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any(),
                any(),
            )
        assertEquals(listOf(OTHER_MEMBER_ID), authorIds.firstValue)
    }

    @Test
    fun `should return an empty page without querying when no requested member belongs to the group`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
        given(groupFacade.getMemberIds(GROUP_ID)).willReturn(listOf(USER_ID))
        val foreignId = UUID.fromString("00000000-0000-0000-0000-0000000000ff")

        // When
        val result =
            searchGroupPublicationsUseCase.execute(
                email = EMAIL,
                groupId = GROUP_ID,
                memberIds = listOf(foreignId),
                title = null,
                journalId = null,
                status = null,
                minDaysInStatus = null,
                page = 0,
                size = 20,
            )

        // Then
        assertEquals(0, result.totalElements)
        assertTrue(result.items.isEmpty())
        verify(publicationRepository, never())
            .searchByGroup(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), any())
    }

    @Test
    fun `should apply the stale filter and exclude final statuses when minDaysInStatus is set`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
        given(
                publicationRepository.searchByGroup(
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                )
            )
            .willReturn(PageResult(emptyList(), totalElements = 0, page = 0, size = 20))

        // When
        searchGroupPublicationsUseCase.execute(
            email = EMAIL,
            groupId = GROUP_ID,
            memberIds = null,
            title = null,
            journalId = null,
            status = null,
            minDaysInStatus = 30,
            page = 0,
            size = 20,
        )

        // Then
        val staleBefore = argumentCaptor<Instant>()
        val excludeFinal = argumentCaptor<Boolean>()
        verify(publicationRepository)
            .searchByGroup(
                eq(GROUP_ID),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                staleBefore.capture(),
                excludeFinal.capture(),
                any(),
                any(),
            )
        assertTrue(excludeFinal.firstValue)
        assertNotNull(staleBefore.firstValue)
    }
}
