package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
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
class DeleteGroupUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val MEMBER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OUTSIDER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private const val ADMIN_EMAIL = "admin@test.com"
        private const val MEMBER_EMAIL = "member@test.com"
        private const val OUTSIDER_EMAIL = "outsider@test.com"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var invitationRepository: InvitationRepository

    @Mock private lateinit var userFacade: UserFacade

    @InjectMocks private lateinit var deleteGroupUseCase: DeleteGroupUseCase

    @Test
    fun `should delete the group and its invitations when acting user is admin`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When
        deleteGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID)

        // Then
        verify(invitationRepository).deleteAllByGroupId(GROUP_ID)
        verify(groupRepository).delete(GROUP_ID)
    }

    @Test
    fun `should throw GroupNotFoundException when the group does not exist`() {
        // Given
        given(groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> { deleteGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID) }
        verify(invitationRepository, never()).deleteAllByGroupId(any())
        verify(groupRepository, never()).delete(any())
    }

    @Test
    fun `should throw NotGroupMemberException when the acting user is not a member`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(OUTSIDER_EMAIL)).willReturn(OUTSIDER_USER_ID)

        // When + Then
        assertThrows<NotGroupMemberException> {
            deleteGroupUseCase.execute(OUTSIDER_EMAIL, GROUP_ID)
        }
        verify(invitationRepository, never()).deleteAllByGroupId(any())
        verify(groupRepository, never()).delete(any())
    }

    @Test
    fun `should throw GroupMemberNotAdminException when the acting user is not an admin`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(MEMBER_EMAIL)).willReturn(MEMBER_USER_ID)

        // When + Then
        assertThrows<GroupMemberNotAdminException> {
            deleteGroupUseCase.execute(MEMBER_EMAIL, GROUP_ID)
        }
        verify(invitationRepository, never()).deleteAllByGroupId(any())
        verify(groupRepository, never()).delete(any())
    }

    private fun createGroup() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_USER_ID).apply {
            addMember(ADMIN_USER_ID, GroupRole.ADMIN)
            addMember(MEMBER_USER_ID, GroupRole.MEMBER)
        }
}
