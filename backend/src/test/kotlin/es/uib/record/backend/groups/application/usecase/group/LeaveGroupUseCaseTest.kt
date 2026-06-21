package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.LastGroupAdminException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertFalse
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
class LeaveGroupUseCaseTest {

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

    @InjectMocks private lateinit var leaveGroupUseCase: LeaveGroupUseCase

    @Test
    fun `should let a member leave the group successfully`() {
        // Given
        val group = createGroupWithAdminAndMember()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(MEMBER_EMAIL)).willReturn(MEMBER_USER_ID)

        // When
        leaveGroupUseCase.execute(MEMBER_EMAIL, GROUP_ID)

        // Then
        val captor = argumentCaptor<Group>()
        verify(groupRepository).save(captor.capture())
        assertFalse(captor.firstValue.isMember(MEMBER_USER_ID))
    }

    @Test
    fun `should let an admin leave when another admin exists`() {
        // Given
        val group = createGroupWithTwoAdmins()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When
        leaveGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID)

        // Then
        val captor = argumentCaptor<Group>()
        verify(groupRepository).save(captor.capture())
        assertFalse(captor.firstValue.isMember(ADMIN_USER_ID))
    }

    @Test
    fun `should throw GroupNotFoundException when the group does not exist`() {
        // Given
        given(groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> { leaveGroupUseCase.execute(MEMBER_EMAIL, GROUP_ID) }
        verify(groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw NotGroupMemberException when the acting user is not a member`() {
        // Given
        val group = createGroupWithAdminAndMember()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(OUTSIDER_EMAIL)).willReturn(OUTSIDER_USER_ID)

        // When + Then
        assertThrows<NotGroupMemberException> {
            leaveGroupUseCase.execute(OUTSIDER_EMAIL, GROUP_ID)
        }
        verify(groupRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should throw LastGroupAdminException when the last admin tries to leave`() {
        // Given
        val group = createGroupWithAdminAndMember()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)

        // When + Then
        assertThrows<LastGroupAdminException> { leaveGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID) }
        verify(groupRepository, never()).save(org.mockito.kotlin.any())
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
