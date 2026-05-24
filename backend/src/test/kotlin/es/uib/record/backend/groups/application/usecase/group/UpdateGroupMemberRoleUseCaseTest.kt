package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.LastGroupAdminException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class UpdateGroupMemberRoleUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val OTHER_ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val MEMBER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val OUTSIDER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private const val ADMIN_EMAIL = "admin@test.com"
        private const val MEMBER_EMAIL = "member@test.com"
        private const val OUTSIDER_EMAIL = "outsider@test.com"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var userFacade: UserFacade

    @InjectMocks private lateinit var updateGroupMemberRoleUseCase: UpdateGroupMemberRoleUseCase

    @Test
    fun `should promote a member to admin successfully`() {
        // Given
        val group = this.createGroupWithAdminAndMember()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When
        this.updateGroupMemberRoleUseCase.execute(
            ADMIN_EMAIL,
            GROUP_ID,
            MEMBER_USER_ID,
            GroupRole.ADMIN,
        )

        // Then
        val captor = argumentCaptor<Group>()
        verify(this.groupRepository).save(captor.capture())
        assertEquals(GroupRole.ADMIN, captor.firstValue.getMemberRole(MEMBER_USER_ID))
    }

    @Test
    fun `should downgrade an admin to member when there are more admins`() {
        // Given
        val group = this.createGroupWithTwoAdmins()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When
        this.updateGroupMemberRoleUseCase.execute(
            ADMIN_EMAIL,
            GROUP_ID,
            OTHER_ADMIN_USER_ID,
            GroupRole.MEMBER,
        )

        // Then
        val captor = argumentCaptor<Group>()
        verify(this.groupRepository).save(captor.capture())
        assertEquals(GroupRole.MEMBER, captor.firstValue.getMemberRole(OTHER_ADMIN_USER_ID))
    }

    @Test
    fun `should allow an admin to self-demote when another admin exists`() {
        // Given
        val group = this.createGroupWithTwoAdmins()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When
        this.updateGroupMemberRoleUseCase.execute(
            ADMIN_EMAIL,
            GROUP_ID,
            ADMIN_USER_ID,
            GroupRole.MEMBER,
        )

        // Then
        val captor = argumentCaptor<Group>()
        verify(this.groupRepository).save(captor.capture())
        assertEquals(GroupRole.MEMBER, captor.firstValue.getMemberRole(ADMIN_USER_ID))
    }

    @Test
    fun `should throw GroupNotFoundException when the group does not exist`() {
        // Given
        given(this.groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> {
            this.updateGroupMemberRoleUseCase.execute(
                ADMIN_EMAIL,
                GROUP_ID,
                MEMBER_USER_ID,
                GroupRole.ADMIN,
            )
        }
        verify(this.groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw NotGroupMemberException when the acting user is not a member`() {
        // Given
        val group = this.createGroupWithAdminAndMember()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(OUTSIDER_EMAIL)).willReturn(OUTSIDER_USER_ID)

        // When + Then
        assertThrows<NotGroupMemberException> {
            this.updateGroupMemberRoleUseCase.execute(
                OUTSIDER_EMAIL,
                GROUP_ID,
                MEMBER_USER_ID,
                GroupRole.ADMIN,
            )
        }
        verify(this.groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw GroupMemberNotAdminException when the acting user is not admin`() {
        // Given
        val group = this.createGroupWithAdminAndMember()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(MEMBER_EMAIL)).willReturn(MEMBER_USER_ID)

        // When + Then
        assertThrows<GroupMemberNotAdminException> {
            this.updateGroupMemberRoleUseCase.execute(
                MEMBER_EMAIL,
                GROUP_ID,
                ADMIN_USER_ID,
                GroupRole.MEMBER,
            )
        }
        verify(this.groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw NotGroupMemberException when the target user is not a member`() {
        // Given
        val group = this.createGroupWithAdminAndMember()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When + Then
        assertThrows<NotGroupMemberException> {
            this.updateGroupMemberRoleUseCase.execute(
                ADMIN_EMAIL,
                GROUP_ID,
                OUTSIDER_USER_ID,
                GroupRole.ADMIN,
            )
        }
        verify(this.groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw LastGroupAdminException when downgrading the only admin`() {
        // Given
        val group = this.createGroupWithAdminAndMember()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When + Then (admin self-demote attempt while being the only admin)
        assertThrows<LastGroupAdminException> {
            this.updateGroupMemberRoleUseCase.execute(
                ADMIN_EMAIL,
                GROUP_ID,
                ADMIN_USER_ID,
                GroupRole.MEMBER,
            )
        }
        verify(this.groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should return without saving when the target already has the requested role`() {
        // Given
        val group = this.createGroupWithAdminAndMember()
        given(this.groupRepository.findById(GROUP_ID)).willReturn(group)
        given(this.userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When
        this.updateGroupMemberRoleUseCase.execute(
            ADMIN_EMAIL,
            GROUP_ID,
            MEMBER_USER_ID,
            GroupRole.MEMBER,
        )

        // Then
        verify(this.groupRepository, never()).save(org.mockito.kotlin.any())
    }

    private fun createGroupWithAdminAndMember() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_USER_ID).apply {
            addMember(ADMIN_USER_ID, GroupRole.ADMIN)
            addMember(MEMBER_USER_ID, GroupRole.MEMBER)
        }

    private fun createGroupWithTwoAdmins() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_USER_ID).apply {
            addMember(ADMIN_USER_ID, GroupRole.ADMIN)
            addMember(OTHER_ADMIN_USER_ID, GroupRole.ADMIN)
        }
}
