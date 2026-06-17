package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.application.usecase.group.dto.InvitableUserResponseDto
import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.model.Invitation
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
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

@ExtendWith(MockitoExtension::class)
class SearchInvitableUsersUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val NON_MEMBER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val INVITEE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val MEMBER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private val PENDING_INVITEE_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000006")
        private const val USER_EMAIL = "test@test.com"
        private const val QUERY = "query"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var invitationRepository: InvitationRepository

    @InjectMocks private lateinit var searchInvitableUsersUseCase: SearchInvitableUsersUseCase

    @Test
    fun `should return mapped invitable users when none are excluded`() {
        // Given
        val group = this.createGroup()
        val user1 = this.createUserOpenDto(INVITEE_USER_ID)
        val user2 = this.createUserOpenDto(NON_MEMBER_USER_ID)

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)
        given(this.invitationRepository.findByGroupId(GROUP_ID)).willReturn(emptyList())
        given(this.userFacade.searchUsers(QUERY)).willReturn(listOf(user1, user2))

        // When
        val result = this.searchInvitableUsersUseCase.execute(USER_EMAIL, GROUP_ID, QUERY)

        // Then
        assertEquals(
            listOf(this.expectedDto(INVITEE_USER_ID), this.expectedDto(NON_MEMBER_USER_ID)),
            result,
        )
    }

    @Test
    fun `should exclude group members and pending invitees from results`() {
        // Given
        val group = this.createGroup().apply { addMember(MEMBER_USER_ID) }
        val memberUser = this.createUserOpenDto(MEMBER_USER_ID)
        val pendingUser = this.createUserOpenDto(PENDING_INVITEE_USER_ID)
        val invitableUser = this.createUserOpenDto(INVITEE_USER_ID)

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)
        given(this.invitationRepository.findByGroupId(GROUP_ID))
            .willReturn(listOf(this.createInvitation(PENDING_INVITEE_USER_ID)))
        given(this.userFacade.searchUsers(QUERY))
            .willReturn(listOf(memberUser, pendingUser, invitableUser))

        // When
        val result = this.searchInvitableUsersUseCase.execute(USER_EMAIL, GROUP_ID, QUERY)

        // Then
        assertEquals(listOf(this.expectedDto(INVITEE_USER_ID)), result)
    }

    @Test
    fun `should return empty list when search has no matches`() {
        // Given
        val group = this.createGroup()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)
        given(this.invitationRepository.findByGroupId(GROUP_ID)).willReturn(emptyList())
        given(this.userFacade.searchUsers(QUERY)).willReturn(emptyList())

        // When
        val result = this.searchInvitableUsersUseCase.execute(USER_EMAIL, GROUP_ID, QUERY)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should throw GroupNotFoundException when group does not exist`() {
        // Given
        given(this.groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> {
            this.searchInvitableUsersUseCase.execute(USER_EMAIL, GROUP_ID, QUERY)
        }
    }

    @Test
    fun `should throw NotGroupMemberException when the user is not a member of the group`() {
        // Given
        val group = this.createGroup()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(NON_MEMBER_USER_ID)

        // When + Then
        assertThrows<NotGroupMemberException> {
            this.searchInvitableUsersUseCase.execute(USER_EMAIL, GROUP_ID, QUERY)
        }
    }

    @Test
    fun `should throw GroupMemberNotAdminException when the user is not an admin`() {
        // Given
        val group =
            Group(id = GROUP_ID, name = "Test Group", createdBy = USER_ID).apply {
                addMember(USER_ID, GroupRole.MEMBER)
            }
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)

        // When + Then
        assertThrows<GroupMemberNotAdminException> {
            this.searchInvitableUsersUseCase.execute(USER_EMAIL, GROUP_ID, QUERY)
        }
    }

    private fun createGroup() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = USER_ID).apply {
            addMember(USER_ID, GroupRole.ADMIN)
        }

    private fun createInvitation(inviteeUserId: UUID) =
        Invitation(groupId = GROUP_ID, inviteeUserId = inviteeUserId, inviterUserId = USER_ID)

    private fun createUserOpenDto(userId: UUID) =
        UserOpenDto(
            userId = userId,
            firstName = "First-$userId",
            lastName = "Last-$userId",
            email = "$userId@test.com",
            profileImageUrl = "https://example.com/$userId.png",
        )

    private fun expectedDto(userId: UUID) =
        InvitableUserResponseDto(
            id = userId,
            firstName = "First-$userId",
            lastName = "Last-$userId",
            email = "$userId@test.com",
            profileImageUrl = "https://example.com/$userId.png",
        )
}
