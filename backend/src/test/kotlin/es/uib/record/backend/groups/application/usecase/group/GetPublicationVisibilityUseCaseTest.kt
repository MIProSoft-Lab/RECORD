package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class GetPublicationVisibilityUseCaseTest {

    companion object {
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OTHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private const val USER_EMAIL = "user@test.com"
        private const val OUTSIDER_EMAIL = "outsider@test.com"
    }

    @Mock private lateinit var groupRepository: GroupRepository

    @Mock private lateinit var publicationVisibilityRepository: PublicationVisibilityRepository

    @Mock private lateinit var userFacade: UserFacade

    @InjectMocks
    private lateinit var getPublicationVisibilityUseCase: GetPublicationVisibilityUseCase

    @Test
    fun `should mark hidden members as not visible and admins as locked`() {
        // Given the current user hid OTHER; ADMIN is always visible and locked
        val group = groupWithAdminUserAndOther()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(USER_EMAIL)).willReturn(USER_ID)
        given(publicationVisibilityRepository.findViewersHiddenByOwner(GROUP_ID, USER_ID))
            .willReturn(setOf(OTHER_ID))
        given(userFacade.getUsersByIds(listOf(ADMIN_ID, OTHER_ID)))
            .willReturn(
                listOf(
                    UserOpenDto(ADMIN_ID, "Ada", "Admin", "admin@test.com", "img"),
                    UserOpenDto(OTHER_ID, "Otto", "Other", "other@test.com", "img"),
                )
            )

        // When
        val result = getPublicationVisibilityUseCase.execute(USER_EMAIL, GROUP_ID)

        // Then (the current user is excluded from the list)
        assertEquals(2, result.size)
        val admin = result.first { it.userId == ADMIN_ID }
        assertTrue(admin.canSee)
        assertTrue(admin.locked)
        val other = result.first { it.userId == OTHER_ID }
        assertFalse(other.canSee)
        assertFalse(other.locked)
    }

    @Test
    fun `should throw GroupNotFoundException when the group does not exist`() {
        given(groupRepository.findById(GROUP_ID)).willReturn(null)

        assertThrows<GroupNotFoundException> {
            getPublicationVisibilityUseCase.execute(USER_EMAIL, GROUP_ID)
        }
    }

    @Test
    fun `should throw NotGroupMemberException when the user is not a member`() {
        val group = groupWithAdminUserAndOther()
        given(groupRepository.findById(GROUP_ID)).willReturn(group)
        given(userFacade.getUserIdByEmail(OUTSIDER_EMAIL))
            .willReturn(UUID.fromString("00000000-0000-0000-0000-0000000000ff"))

        assertThrows<NotGroupMemberException> {
            getPublicationVisibilityUseCase.execute(OUTSIDER_EMAIL, GROUP_ID)
        }
    }

    private fun groupWithAdminUserAndOther() =
        Group(id = GROUP_ID, name = "Test Group", createdBy = ADMIN_ID).apply {
            addMember(ADMIN_ID, GroupRole.ADMIN)
            addMember(USER_ID, GroupRole.MEMBER)
            addMember(OTHER_ID, GroupRole.MEMBER)
        }
}
