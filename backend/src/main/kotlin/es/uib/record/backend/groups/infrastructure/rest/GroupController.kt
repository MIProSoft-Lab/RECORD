package es.uib.record.backend.groups.infrastructure.rest

import es.uib.record.backend.api.GroupsApi
import es.uib.record.backend.groups.application.usecase.group.CreateGroupUseCase
import es.uib.record.backend.groups.application.usecase.group.DeleteGroupUseCase
import es.uib.record.backend.groups.application.usecase.group.GetGroupDetailUseCase
import es.uib.record.backend.groups.application.usecase.group.GetGroupJournalInterestsUseCase
import es.uib.record.backend.groups.application.usecase.group.GetGroupsListByMemberIdUseCase
import es.uib.record.backend.groups.application.usecase.group.KickGroupMemberUseCase
import es.uib.record.backend.groups.application.usecase.group.LeaveGroupUseCase
import es.uib.record.backend.groups.application.usecase.group.SearchInvitableUsersUseCase
import es.uib.record.backend.groups.application.usecase.group.SendInvitationUseCase
import es.uib.record.backend.groups.application.usecase.group.UpdateGroupMemberRoleUseCase
import es.uib.record.backend.groups.application.usecase.group.UpdateGroupUseCase
import es.uib.record.backend.groups.infrastructure.mapper.toDomain
import es.uib.record.backend.groups.infrastructure.mapper.toDto
import es.uib.record.backend.groups.infrastructure.mapper.toResponse
import es.uib.record.backend.model.CreateGroupRequest
import es.uib.record.backend.model.GroupDetailResponse
import es.uib.record.backend.model.GroupJournalInterestPageResponse
import es.uib.record.backend.model.GroupResponse
import es.uib.record.backend.model.GroupSummaryResponse
import es.uib.record.backend.model.InvitableUserResponse
import es.uib.record.backend.model.SendInvitationRequest
import es.uib.record.backend.model.UpdateGroupMemberRoleRequest
import es.uib.record.backend.model.UpdateGroupRequest
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class GroupController(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getGroupsListByMemberIdUseCase: GetGroupsListByMemberIdUseCase,
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val searchInvitableUsersUseCase: SearchInvitableUsersUseCase,
    private val sendInvitationUseCase: SendInvitationUseCase,
    private val updateGroupMemberRoleUseCase: UpdateGroupMemberRoleUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val kickGroupMemberUseCase: KickGroupMemberUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val getGroupJournalInterestsUseCase: GetGroupJournalInterestsUseCase,
) : GroupsApi {

    override fun createGroup(
        createGroupRequest: CreateGroupRequest
    ): ResponseEntity<GroupResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val createdGroup = this.createGroupUseCase.execute(createGroupRequest.toDto(), email)

        return ResponseEntity.ok(createdGroup.toResponse())
    }

    override fun updateGroup(
        groupId: UUID,
        updateGroupRequest: UpdateGroupRequest,
    ): ResponseEntity<GroupResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val updatedGroup =
            this.updateGroupUseCase.execute(
                email,
                groupId,
                updateGroupRequest.name,
                updateGroupRequest.description,
            )

        return ResponseEntity.ok(updatedGroup.toResponse())
    }

    override fun deleteGroup(groupId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.deleteGroupUseCase.execute(email, groupId)

        return ResponseEntity.noContent().build()
    }

    override fun listGroups(): ResponseEntity<List<GroupSummaryResponse>> {
        val email = SecurityContextHolder.getContext().authentication.name
        val groups = this.getGroupsListByMemberIdUseCase.execute(email)

        return ResponseEntity.ok(groups.map { it.toResponse() })
    }

    override fun getGroupDetail(groupId: UUID): ResponseEntity<GroupDetailResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val groupDetail = this.getGroupDetailUseCase.execute(groupId, email)

        return ResponseEntity.ok(groupDetail.toResponse())
    }

    override fun getGroupJournalInterests(
        groupId: UUID,
        page: Int,
        size: Int,
    ): ResponseEntity<GroupJournalInterestPageResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val interests = this.getGroupJournalInterestsUseCase.execute(groupId, email, page, size)

        return ResponseEntity.ok(interests.toResponse())
    }

    override fun getInvitableUsers(
        groupId: UUID,
        query: String,
    ): ResponseEntity<List<InvitableUserResponse>> {
        val email = SecurityContextHolder.getContext().authentication.name
        val invitableUsers = this.searchInvitableUsersUseCase.execute(email, groupId, query)

        return ResponseEntity.ok(invitableUsers.map { it.toResponse() })
    }

    override fun inviteUserToGroup(
        groupId: UUID,
        sendInvitationRequest: SendInvitationRequest,
    ): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.sendInvitationUseCase.execute(email, groupId, sendInvitationRequest.inviteeUserId)

        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    override fun kickGroupMember(groupId: UUID, memberId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.kickGroupMemberUseCase.execute(email, groupId, memberId)
        return ResponseEntity.noContent().build()
    }

    override fun leaveGroup(groupId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.leaveGroupUseCase.execute(email, groupId)
        return ResponseEntity.noContent().build()
    }

    override fun updateGroupMemberRole(
        groupId: UUID,
        memberId: UUID,
        updateGroupMemberRoleRequest: UpdateGroupMemberRoleRequest,
    ): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.updateGroupMemberRoleUseCase.execute(
            email,
            groupId,
            memberId,
            updateGroupMemberRoleRequest.role.toDomain(),
        )

        return ResponseEntity.noContent().build()
    }
}
