package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.journals.open.InterestedJournalCategoryDto
import es.uib.record.backend.journals.open.InterestedJournalDto
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class GetGroupJournalInterestsUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val NON_MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val JOURNAL_1 = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val JOURNAL_2 = UUID.fromString("00000000-0000-0000-0000-0000000000a2")
        private val CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b1")
        private const val EMAIL = "user@uib.es"
        private const val PAGE = 0
        private const val SIZE = 20
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks
    private lateinit var getGroupJournalInterestsUseCase: GetGroupJournalInterestsUseCase

    @Test
    fun `returns the union with favorite counts and hydrated members`() {
        // Given
        val group = this.createGroup()
        val journal1 =
            this.interestedJournal(JOURNAL_1, interestedUserIds = listOf(USER_ID, MEMBER_ID))
        val journal2 = this.interestedJournal(JOURNAL_2, interestedUserIds = listOf(MEMBER_ID))

        given(this.userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(
                this.journalFacade.getJournalsInterestedByUsers(
                    setOf(USER_ID, MEMBER_ID),
                    PAGE,
                    SIZE,
                )
            )
            .willReturn(
                PageResult(listOf(journal1, journal2), totalElements = 2, page = PAGE, size = SIZE)
            )
        given(this.userFacade.getUsersByIds(listOf(USER_ID, MEMBER_ID)))
            .willReturn(listOf(this.userOpenDto(USER_ID), this.userOpenDto(MEMBER_ID)))

        // When
        val result = this.getGroupJournalInterestsUseCase.execute(GROUP_ID, EMAIL, PAGE, SIZE)

        // Then
        assertEquals(2, result.totalElements)
        assertEquals(2, result.items.size)

        val first = result.items[0]
        assertEquals(JOURNAL_1, first.id)
        assertEquals(2, first.favoriteCount)
        assertEquals(listOf(USER_ID, MEMBER_ID), first.members.map { it.userId })
        assertEquals("Q1", first.categories.single().quartile)

        val second = result.items[1]
        assertEquals(JOURNAL_2, second.id)
        assertEquals(1, second.favoriteCount)
        assertEquals(listOf(MEMBER_ID), second.members.map { it.userId })
    }

    @Test
    fun `hydrates members with a single call to getUsersByIds`() {
        // Given
        val group = this.createGroup()
        val journal1 =
            this.interestedJournal(JOURNAL_1, interestedUserIds = listOf(USER_ID, MEMBER_ID))
        val journal2 = this.interestedJournal(JOURNAL_2, interestedUserIds = listOf(MEMBER_ID))

        given(this.userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(
                this.journalFacade.getJournalsInterestedByUsers(
                    setOf(USER_ID, MEMBER_ID),
                    PAGE,
                    SIZE,
                )
            )
            .willReturn(
                PageResult(listOf(journal1, journal2), totalElements = 2, page = PAGE, size = SIZE)
            )
        given(this.userFacade.getUsersByIds(listOf(USER_ID, MEMBER_ID)))
            .willReturn(listOf(this.userOpenDto(USER_ID), this.userOpenDto(MEMBER_ID)))

        // When
        this.getGroupJournalInterestsUseCase.execute(GROUP_ID, EMAIL, PAGE, SIZE)

        // Then
        verify(this.userFacade, times(1)).getUsersByIds(listOf(USER_ID, MEMBER_ID))
    }

    @Test
    fun `returns an empty page when no member has marked any journal`() {
        // Given
        val group = this.createGroup()
        given(this.userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(
                this.journalFacade.getJournalsInterestedByUsers(
                    setOf(USER_ID, MEMBER_ID),
                    PAGE,
                    SIZE,
                )
            )
            .willReturn(PageResult(emptyList(), totalElements = 0, page = PAGE, size = SIZE))
        given(this.userFacade.getUsersByIds(emptyList())).willReturn(emptyList())

        // When
        val result = this.getGroupJournalInterestsUseCase.execute(GROUP_ID, EMAIL, PAGE, SIZE)

        // Then
        assertTrue(result.items.isEmpty())
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `throws GroupNotFoundException when the group does not exist`() {
        // Given
        given(this.userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(this.groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> {
            this.getGroupJournalInterestsUseCase.execute(GROUP_ID, EMAIL, PAGE, SIZE)
        }
    }

    @Test
    fun `throws NotGroupMemberException when the user is not a member of the group`() {
        // Given
        given(this.userFacade.getUserIdByEmail(EMAIL)).willReturn(NON_MEMBER_ID)
        given(this.groupRepository.findById(GROUP_ID)).willReturn(this.createGroup())

        // When + Then
        assertThrows<NotGroupMemberException> {
            this.getGroupJournalInterestsUseCase.execute(GROUP_ID, EMAIL, PAGE, SIZE)
        }
    }

    private fun createGroup() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = USER_ID).apply {
            addMember(USER_ID, GroupRole.ADMIN)
            addMember(MEMBER_ID, GroupRole.MEMBER)
        }

    private fun interestedJournal(journalId: UUID, interestedUserIds: List<UUID>) =
        InterestedJournalDto(
            journalId = journalId,
            name = "Journal-$journalId",
            issn = "0000-0001",
            eIssn = "0000-0002",
            publisherName = "Publisher",
            year = 2023,
            categories =
                listOf(
                    InterestedJournalCategoryDto(
                        categoryId = CATEGORY_ID,
                        categoryName = "ONCOLOGY",
                        edition = "SCIE",
                        quartile = "Q1",
                        impactFactor = 12.3,
                    )
                ),
            interestedUserIds = interestedUserIds,
        )

    private fun userOpenDto(userId: UUID) =
        UserOpenDto(
            userId = userId,
            firstName = "First-$userId",
            lastName = "Last-$userId",
            email = "$userId@test.com",
            profileImageUrl = "https://example.com/$userId.png",
        )
}
