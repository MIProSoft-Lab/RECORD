package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class GetPublicationDetailUseCaseTest {

    companion object {
        private val PUBLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val CREATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var journalFacade: JournalFacade

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var groupFacade: GroupFacade

    @InjectMocks private lateinit var getPublicationDetailUseCase: GetPublicationDetailUseCase

    private fun publication(createdBy: UUID = CREATOR_ID) =
        Publication(
            id = PUBLICATION_ID,
            title = "First",
            journalId = JOURNAL_ID,
            groupId = GROUP_ID,
            status = PublicationStatus.PLANNED,
            createdBy = createdBy,
        )

    @Test
    fun `should return the publication detail with the journal name when visible`() {
        // Given a member to whom the creator does not hide the publication
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(publication())
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID)))
            .willReturn(mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null)))

        // When
        val result = getPublicationDetailUseCase.execute(PUBLICATION_ID, EMAIL)

        // Then
        assertEquals(PUBLICATION_ID, result.id)
        assertEquals("First", result.title)
        assertEquals("Nature", result.journalName)
    }

    @Test
    fun `should throw PublicationNotFoundException when the publication does not exist`() {
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(null)

        assertThrows<PublicationNotFoundException> {
            getPublicationDetailUseCase.execute(PUBLICATION_ID, EMAIL)
        }
    }

    @Test
    fun `should throw UserNotGroupMemberException when the user is not a group member`() {
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(publication())
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(false)

        assertThrows<UserNotGroupMemberException> {
            getPublicationDetailUseCase.execute(PUBLICATION_ID, EMAIL)
        }
    }

    @Test
    fun `should hide the detail when every author hid it from a non-admin viewer`() {
        // Given the only author (creator) hides the publication from the viewer
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(publication())
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
        given(groupFacade.getOwnersHiddenFromViewer(GROUP_ID, USER_ID))
            .willReturn(setOf(CREATOR_ID))

        assertThrows<PublicationNotFoundException> {
            getPublicationDetailUseCase.execute(PUBLICATION_ID, EMAIL)
        }
    }
}
