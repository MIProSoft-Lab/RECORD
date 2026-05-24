package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.AlreadyGroupMemberException
import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.exception.UserAlreadyInvitedException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.model.Invitation
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class SendInvitationUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val INVITER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val INVITER_USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val INVITEE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private const val USER_EMAIL = "test@test.com"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var invitationRepository: InvitationRepository

    @InjectMocks private lateinit var sendInvitationUseCase: SendInvitationUseCase

    @Test
    fun `should create an invitation successfully`() {
        // Given
        val group = this.createGroup()

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(INVITER_USER_ID)
        given(this.invitationRepository.findByGroupId(GROUP_ID)).willReturn(emptyList())

        // When
        this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)

        // Then
        val captor = argumentCaptor<Invitation>()
        verify(this.invitationRepository).save(captor.capture())
        val invitation = captor.firstValue
        assertEquals(invitation.inviterUserId, INVITER_USER_ID)
        assertEquals(invitation.groupId, GROUP_ID)
        assertEquals(invitation.inviteeUserId, INVITEE_USER_ID)
    }

    @Test
    fun `should throw GroupNotFoundException when group does not exist`() {
        // Given
        given(this.groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> {
            this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)
        }
    }

    @Test
    fun `should throw NotGroupMemberException when the inviter is not a member of the group`() {
        // Given
        val group = this.createGroup()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(INVITER_USER_ID_2)

        // When + Then
        assertThrows<NotGroupMemberException> {
            this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)
        }
    }

    @Test
    fun `should throw GroupMemberNotAdminException when the inviter doesn't have admin role`() {
        // Given
        val group = this.createGroup()
        group.updateMemberRole(INVITER_USER_ID, GroupRole.MEMBER)

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(INVITER_USER_ID)

        // When + Then
        assertThrows<GroupMemberNotAdminException> {
            this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)
        }
    }

    @Test
    fun `should throw UserNotFoundException when the invitee does not exist`() {
        // Given
        val group = this.createGroup()

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(INVITER_USER_ID)
        given(this.userFacade.getUserById(INVITEE_USER_ID))
            .willThrow(UserNotFoundException(INVITEE_USER_ID))

        // When + Then
        assertThrows<UserNotFoundException> {
            this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)
        }
    }

    @Test
    fun `should throw AlreadyGroupMemberException when the invited user is already a member of the group`() {
        // Given
        val group = this.createGroup()
        group.addMember(INVITEE_USER_ID)

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(INVITER_USER_ID)

        // When + Then
        assertThrows<AlreadyGroupMemberException> {
            this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)
        }
    }

    @Test
    fun `should throw UserAlreadyInvitedException when the invited user was already invited to the group`() {
        // Given
        val group = this.createGroup()

        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(INVITER_USER_ID)
        given(this.invitationRepository.findByGroupId(GROUP_ID))
            .willReturn(listOf(this.createInvitation()))

        // When + Then
        assertThrows<UserAlreadyInvitedException> {
            this.sendInvitationUseCase.execute(USER_EMAIL, GROUP_ID, INVITEE_USER_ID)
        }
    }

    private fun createInvitation() =
        Invitation(
            groupId = GROUP_ID,
            inviteeUserId = INVITEE_USER_ID,
            inviterUserId = INVITER_USER_ID,
        )

    private fun createGroup() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = INVITER_USER_ID).apply {
            addMember(INVITER_USER_ID, GroupRole.ADMIN)
        }
}
