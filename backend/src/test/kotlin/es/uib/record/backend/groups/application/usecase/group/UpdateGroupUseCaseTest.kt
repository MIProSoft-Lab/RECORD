package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNameAlreadyExistsException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class UpdateGroupUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val MEMBER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OUTSIDER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val OTHER_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private const val ADMIN_EMAIL = "admin@test.com"
        private const val MEMBER_EMAIL = "member@test.com"
        private const val OUTSIDER_EMAIL = "outsider@test.com"
        private const val NEW_NAME = "Updated Group"
        private const val NEW_DESCRIPTION = "Updated description"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var userFacade: UserFacade

    @InjectMocks private lateinit var updateGroupUseCase: UpdateGroupUseCase

    @Test
    fun `should update name and description successfully when acting user is admin`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)
        given(groupRepository.findByName(NEW_NAME)).willReturn(null)
        given(groupRepository.save(any())).willAnswer { it.arguments[0] as Group }

        // When
        updateGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID, NEW_NAME, NEW_DESCRIPTION)

        // Then
        val captor = argumentCaptor<Group>()
        verify(groupRepository).save(captor.capture())
        assertEquals(NEW_NAME, captor.firstValue.name)
        assertEquals(NEW_DESCRIPTION, captor.firstValue.description)
        assertEquals(GROUP_ID, captor.firstValue.id)
        assertEquals(ADMIN_USER_ID, captor.firstValue.createdBy)
    }

    @Test
    fun `should update successfully when the name is unchanged (own group)`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)
        given(groupRepository.findByName("Test Group")).willReturn(group)
        given(groupRepository.save(any())).willAnswer { it.arguments[0] as Group }

        // When
        updateGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID, "Test Group", NEW_DESCRIPTION)

        // Then
        verify(groupRepository).save(any())
    }

    @Test
    fun `should throw GroupNameAlreadyExistsException when the name belongs to another group`() {
        // Given
        val group = createGroup()
        val otherGroup = Group(id = OTHER_GROUP_ID, name = NEW_NAME, createdBy = MEMBER_USER_ID)
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(ADMIN_EMAIL)).willReturn(ADMIN_USER_ID)
        given(groupRepository.findByName(NEW_NAME)).willReturn(otherGroup)

        // When + Then
        assertThrows<GroupNameAlreadyExistsException> {
            updateGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID, NEW_NAME, NEW_DESCRIPTION)
        }
        verify(groupRepository, never()).save(any())
    }

    @Test
    fun `should throw GroupNotFoundException when the group does not exist`() {
        // Given
        given(groupRepository.findById(GROUP_ID)).willReturn(null)

        // When + Then
        assertThrows<GroupNotFoundException> {
            updateGroupUseCase.execute(ADMIN_EMAIL, GROUP_ID, NEW_NAME, NEW_DESCRIPTION)
        }
        verify(groupRepository, never()).save(any())
    }

    @Test
    fun `should throw NotGroupMemberException when the acting user is not a member`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(OUTSIDER_EMAIL)).willReturn(OUTSIDER_USER_ID)

        // When + Then
        assertThrows<NotGroupMemberException> {
            updateGroupUseCase.execute(OUTSIDER_EMAIL, GROUP_ID, NEW_NAME, NEW_DESCRIPTION)
        }
        verify(groupRepository, never()).save(any())
    }

    @Test
    fun `should throw GroupMemberNotAdminException when the acting user is not an admin`() {
        // Given
        val group = createGroup()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(MEMBER_EMAIL)).willReturn(MEMBER_USER_ID)

        // When + Then
        assertThrows<GroupMemberNotAdminException> {
            updateGroupUseCase.execute(MEMBER_EMAIL, GROUP_ID, NEW_NAME, NEW_DESCRIPTION)
        }
        verify(groupRepository, never()).save(any())
    }

    private fun createGroup() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_USER_ID).apply {
            addMember(ADMIN_USER_ID, GroupRole.ADMIN)
            addMember(MEMBER_USER_ID, GroupRole.MEMBER)
        }
}
