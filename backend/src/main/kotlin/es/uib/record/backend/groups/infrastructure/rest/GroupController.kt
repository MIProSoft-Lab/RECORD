package es.uib.record.backend.groups.infrastructure.rest

import es.uib.record.backend.api.GroupsApi
import es.uib.record.backend.groups.application.usecase.group.CreateGroupUseCase
import es.uib.record.backend.groups.application.usecase.group.GetGroupDetailUseCase
import es.uib.record.backend.groups.application.usecase.group.GetGroupsListByMemberIdUseCase
import es.uib.record.backend.groups.application.usecase.group.SearchInvitableUsersUseCase
import es.uib.record.backend.groups.application.usecase.group.SendInvitationUseCase
import es.uib.record.backend.groups.infrastructure.mapper.toDto
import es.uib.record.backend.groups.infrastructure.mapper.toResponse
import es.uib.record.backend.model.CreateGroupRequest
import es.uib.record.backend.model.GroupDetailResponse
import es.uib.record.backend.model.GroupResponse
import es.uib.record.backend.model.GroupSummaryResponse
import es.uib.record.backend.model.InvitableUserResponse
import es.uib.record.backend.model.SendInvitationRequest
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
) : GroupsApi {

    override fun createGroup(
        createGroupRequest: CreateGroupRequest
    ): ResponseEntity<GroupResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val createdGroup = this.createGroupUseCase.execute(createGroupRequest.toDto(), email)

        return ResponseEntity.ok(createdGroup.toResponse())
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
}
