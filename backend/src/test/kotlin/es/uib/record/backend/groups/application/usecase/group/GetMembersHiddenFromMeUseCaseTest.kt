package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class GetMembersHiddenFromMeUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OTHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private const val USER_EMAIL = "user@test.com"
        private const val ADMIN_EMAIL = "admin@test.com"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var publicationVisibilityRepository: PublicationVisibilityRepository

    @Mock private lateinit var userFacade: UserFacade

    @InjectMocks private lateinit var getMembersHiddenFromMeUseCase: GetMembersHiddenFromMeUseCase

    @Test
    fun `should return the owners who hid their publications from a non-admin member`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)
        given(publicationVisibilityRepository.findOwnersHiddenFromViewer(GROUP_ID, USER_ID))
            .willReturn(setOf(OTHER_ID))

        val result = getMembersHiddenFromMeUseCase.execute(USER_EMAIL, GROUP_ID)

        assertEquals(setOf(OTHER_ID), result)
    }

    @Test
    fun `should return empty for an admin without querying visibility`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_ID)

        val result = getMembersHiddenFromMeUseCase.execute(ADMIN_EMAIL, GROUP_ID)

        assertTrue(result.isEmpty())
        verify(publicationVisibilityRepository, never()).findOwnersHiddenFromViewer(any(), any())
    }

    @Test
    fun `should throw NotGroupMemberException when the user is not a member`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail("outsider@test.com"))
            .willReturn(UUID.fromString("00000000-0000-0000-0000-0000000000ff"))

        assertThrows<NotGroupMemberException> {
            getMembersHiddenFromMeUseCase.execute("outsider@test.com", GROUP_ID)
        }
    }

    private fun group() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_ID).apply {
            addMember(ADMIN_ID, GroupRole.ADMIN)
            addMember(USER_ID, GroupRole.MEMBER)
            addMember(OTHER_ID, GroupRole.MEMBER)
        }
}
