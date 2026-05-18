package es.uib.record.backend.groups.infrastructure.rest

import es.uib.record.backend.api.GroupsApi
import es.uib.record.backend.groups.application.usecase.CreateGroupUseCase
import es.uib.record.backend.groups.application.usecase.GetGroupDetailUseCase
import es.uib.record.backend.groups.application.usecase.GetGroupsListByMemberIdUseCase
import es.uib.record.backend.groups.infrastructure.mapper.toDto
import es.uib.record.backend.groups.infrastructure.mapper.toResponse
import es.uib.record.backend.model.CreateGroupRequest
import es.uib.record.backend.model.GroupDetailResponse
import es.uib.record.backend.model.GroupResponse
import es.uib.record.backend.model.GroupSummaryResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class GroupController(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getGroupsListByMemberIdUseCase: GetGroupsListByMemberIdUseCase,
    private val getGroupDetailUseCase: GetGroupDetailUseCase
) : GroupsApi {

    override fun createGroup(createGroupRequest: CreateGroupRequest): ResponseEntity<GroupResponse> {
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
}