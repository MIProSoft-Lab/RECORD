package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.application.usecase.dto.CreatePublicationRequestDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorInputDto
import es.uib.record.backend.publications.domain.exception.AuthorUserNotFoundException
import es.uib.record.backend.publications.domain.exception.DoiNotAllowedException
import es.uib.record.backend.publications.domain.exception.GroupNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.JournalNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationAuthor
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CreatePublicationUseCaseTest {

    companion object {
        private val PUBLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val COAUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private const val EMAIL = "user@test.com"
        private const val TITLE = "Deep learning approaches"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @Mock private lateinit var groupFacade: GroupFacade

    @InjectMocks private lateinit var createPublicationUseCase: CreatePublicationUseCase

    @Test
    fun `should create the publication with the given status, the creator as sole author and the group assigned`() {
        // Given
        givenValidContext()
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(status = PublicationStatus.SUBMITTED)

        // When
        val result = createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        val saved = captor.firstValue
        assertEquals(TITLE, saved.title)
        assertEquals(JOURNAL_ID, saved.journalId)
        assertEquals(GROUP_ID, saved.groupId)
        assertEquals(USER_ID, saved.createdBy)
        assertEquals(PublicationStatus.SUBMITTED, saved.status)
        assertEquals(listOf(PublicationAuthor.InternalAuthor(USER_ID)), saved.authors)
        assertEquals(JOURNAL_ID, result.journalId)
        assertEquals("Nature", result.journalName)
        assertEquals(PublicationStatus.SUBMITTED, result.status)
    }

    @Test
    fun `should default to PLANNED status when no status is provided`() {
        // Given
        givenValidContext()
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(status = null)

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(PublicationStatus.PLANNED, captor.firstValue.status)
    }

    @Test
    fun `should throw JournalNotFoundForPublicationException when the journal does not exist`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalFacade.existsById(JOURNAL_ID)).willReturn(false)

        // When / Then
        assertThrows<JournalNotFoundForPublicationException> {
            createPublicationUseCase.execute(createDto(), EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should throw GroupNotFoundForPublicationException when the group does not exist`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalFacade.existsById(JOURNAL_ID)).willReturn(true)
        given(groupFacade.existsById(GROUP_ID)).willReturn(false)

        // When / Then
        assertThrows<GroupNotFoundForPublicationException> {
            createPublicationUseCase.execute(createDto(), EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should throw UserNotGroupMemberException when the creator is not a member of the group`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalFacade.existsById(JOURNAL_ID)).willReturn(true)
        given(groupFacade.existsById(GROUP_ID)).willReturn(true)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(false)

        // When / Then
        assertThrows<UserNotGroupMemberException> {
            createPublicationUseCase.execute(createDto(), EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should accept the DOI when the status is PUBLISHED`() {
        // Given
        givenValidContext()
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(status = PublicationStatus.PUBLISHED, doi = "10.1000/xyz123")

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals("10.1000/xyz123", captor.firstValue.doi)
    }

    @Test
    fun `should throw DoiNotAllowedException when a DOI is provided with a non-PUBLISHED status`() {
        // Given
        givenValidContext()
        val dto = createDto(status = PublicationStatus.PLANNED, doi = "10.1000/xyz123")

        // When / Then
        assertThrows<DoiNotAllowedException> { createPublicationUseCase.execute(dto, EMAIL) }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should add an internal co-author even if they are not a member of the group`() {
        // Given: the co-author exists but no group membership is required for co-authors.
        givenValidContext()
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID)))
            .willReturn(listOf(userOpenDto(COAUTHOR_ID)))
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(authors = listOf(internalInput(COAUTHOR_ID)))

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(
            listOf(
                PublicationAuthor.InternalAuthor(USER_ID),
                PublicationAuthor.InternalAuthor(COAUTHOR_ID),
            ),
            captor.firstValue.authors,
        )
    }

    @Test
    fun `should add an external co-author by name`() {
        // Given
        givenValidContext()
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(authors = listOf(externalInput("Jane", "Doe")))

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(
            listOf(
                PublicationAuthor.InternalAuthor(USER_ID),
                PublicationAuthor.ExternalAuthor("Jane", "Doe"),
            ),
            captor.firstValue.authors,
        )
    }

    @Test
    fun `should preserve author order with the creator first, then the requested authors`() {
        // Given: an external author followed by an internal one.
        givenValidContext()
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID)))
            .willReturn(listOf(userOpenDto(COAUTHOR_ID)))
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto =
            createDto(authors = listOf(externalInput("Jane", "Doe"), internalInput(COAUTHOR_ID)))

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(
            listOf(
                PublicationAuthor.InternalAuthor(USER_ID),
                PublicationAuthor.ExternalAuthor("Jane", "Doe"),
                PublicationAuthor.InternalAuthor(COAUTHOR_ID),
            ),
            captor.firstValue.authors,
        )
    }

    @Test
    fun `should respect the author order including the creator placed among co-authors`() {
        // Given: the creator (USER_ID) is sent in second position, not forced first.
        givenValidContext()
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID, USER_ID)))
            .willReturn(listOf(userOpenDto(COAUTHOR_ID), userOpenDto(USER_ID)))
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(authors = listOf(internalInput(COAUTHOR_ID), internalInput(USER_ID)))

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(
            listOf(
                PublicationAuthor.InternalAuthor(COAUTHOR_ID),
                PublicationAuthor.InternalAuthor(USER_ID),
            ),
            captor.firstValue.authors,
        )
    }

    @Test
    fun `should throw AuthorUserNotFoundException when an internal co-author does not exist`() {
        // Given
        givenValidContext()
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID))).willReturn(emptyList())
        val dto = createDto(authors = listOf(internalInput(COAUTHOR_ID)))

        // When / Then
        assertThrows<AuthorUserNotFoundException> { createPublicationUseCase.execute(dto, EMAIL) }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should not duplicate the creator when present in the authors list`() {
        // Given
        givenValidContext()
        given(userFacade.getUsersByIds(listOf(USER_ID))).willReturn(listOf(userOpenDto(USER_ID)))
        givenSaveReturnsPersisted()
        givenJournalLookup("Nature")
        val dto = createDto(authors = listOf(internalInput(USER_ID)))

        // When
        createPublicationUseCase.execute(dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(listOf(PublicationAuthor.InternalAuthor(USER_ID)), captor.firstValue.authors)
    }

    private fun givenValidContext() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalFacade.existsById(JOURNAL_ID)).willReturn(true)
        given(groupFacade.existsById(GROUP_ID)).willReturn(true)
        given(groupFacade.isMember(GROUP_ID, USER_ID)).willReturn(true)
    }

    /** Simula la persistencia asignando id a la publicación y a cada autor. */
    private fun givenSaveReturnsPersisted() {
        given(publicationRepository.save(any())).willAnswer {
            val p = it.getArgument<Publication>(0)
            Publication(
                id = PUBLICATION_ID,
                title = p.title,
                abstractText = p.abstractText,
                doi = p.doi,
                journalId = p.journalId,
                groupId = p.groupId,
                status = p.status,
                createdBy = p.createdBy,
                createdAt = p.createdAt,
                authors =
                    p.authors.map { author ->
                        when (author) {
                            is PublicationAuthor.InternalAuthor ->
                                author.copy(id = UUID.randomUUID())
                            is PublicationAuthor.ExternalAuthor ->
                                author.copy(id = UUID.randomUUID())
                        }
                    },
            )
        }
    }

    private fun givenJournalLookup(name: String?) {
        val result =
            if (name == null) emptyMap()
            else mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, name, null))
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID))).willReturn(result)
    }

    private fun userOpenDto(userId: UUID) =
        UserOpenDto(
            userId = userId,
            firstName = "First-$userId",
            lastName = "Last-$userId",
            email = "$userId@test.com",
            profileImageUrl = "https://example.com/$userId.png",
        )

    private fun internalInput(userId: UUID) =
        PublicationAuthorInputDto(userId = userId, firstName = null, lastName = null)

    private fun externalInput(firstName: String, lastName: String) =
        PublicationAuthorInputDto(userId = null, firstName = firstName, lastName = lastName)

    private fun createDto(
        status: PublicationStatus? = PublicationStatus.PLANNED,
        doi: String? = null,
        authors: List<PublicationAuthorInputDto> = emptyList(),
    ) =
        CreatePublicationRequestDto(
            title = TITLE,
            abstractText = "An abstract",
            doi = doi,
            journalId = JOURNAL_ID,
            groupId = GROUP_ID,
            status = status,
            authors = authors,
        )
}
