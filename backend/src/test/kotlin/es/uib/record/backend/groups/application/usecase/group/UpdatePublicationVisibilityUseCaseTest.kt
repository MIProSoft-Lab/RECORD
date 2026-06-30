package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.CannotChangeOwnPublicationVisibilityException
import es.uib.record.backend.groups.domain.exception.CannotHidePublicationsFromAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
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
class UpdatePublicationVisibilityUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OTHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val OUTSIDER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000ff")
        private const val USER_EMAIL = "user@test.com"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var publicationVisibilityRepository: PublicationVisibilityRepository

    @Mock private lateinit var userFacade: UserFacade

    @InjectMocks
    private lateinit var updatePublicationVisibilityUseCase: UpdatePublicationVisibilityUseCase

    @Test
    fun `should hide publications from a member when canSee is false`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)

        updatePublicationVisibilityUseCase.execute(USER_EMAIL, GROUP_ID, OTHER_ID, canSee = false)

        verify(publicationVisibilityRepository).hide(GROUP_ID, USER_ID, OTHER_ID)
    }

    @Test
    fun `should restore visibility for a member when canSee is true`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)

        updatePublicationVisibilityUseCase.execute(USER_EMAIL, GROUP_ID, OTHER_ID, canSee = true)

        verify(publicationVisibilityRepository).unhide(GROUP_ID, USER_ID, OTHER_ID)
    }

    @Test
    fun `should throw when trying to hide publications from an admin`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)

        assertThrows<CannotHidePublicationsFromAdminException> {
            updatePublicationVisibilityUseCase.execute(
                USER_EMAIL,
                GROUP_ID,
                ADMIN_ID,
                canSee = false,
            )
        }
        verify(publicationVisibilityRepository, never()).hide(any(), any(), any())
    }

    @Test
    fun `should throw when changing visibility for oneself`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)

        assertThrows<CannotChangeOwnPublicationVisibilityException> {
            updatePublicationVisibilityUseCase.execute(
                USER_EMAIL,
                GROUP_ID,
                USER_ID,
                canSee = false,
            )
        }
    }

    @Test
    fun `should throw when the target is not a group member`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(group())
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)

        assertThrows<NotGroupMemberException> {
            updatePublicationVisibilityUseCase.execute(
                USER_EMAIL,
                GROUP_ID,
                OUTSIDER_ID,
                canSee = false,
            )
        }
    }

    @Test
    fun `should throw GroupNotFoundException when the group does not exist`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(null)

        assertThrows<GroupNotFoundException> {
            updatePublicationVisibilityUseCase.execute(
                USER_EMAIL,
                GROUP_ID,
                OTHER_ID,
                canSee = false,
            )
        }
    }

    private fun group() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_ID).apply {
            addMember(ADMIN_ID, GroupRole.ADMIN)
            addMember(USER_ID, GroupRole.MEMBER)
            addMember(OTHER_ID, GroupRole.MEMBER)
        }
}
