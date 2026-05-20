package es.uib.record.backend.groups.infrastructure.rest

import es.uib.record.backend.api.InvitationsApi
import es.uib.record.backend.groups.application.usecase.invitation.AcceptInvitationByIdUseCase
import es.uib.record.backend.groups.application.usecase.invitation.GetInvitationsByUserIdUseCase
import es.uib.record.backend.groups.application.usecase.invitation.RejectInvitationByIdUseCase
import es.uib.record.backend.groups.infrastructure.mapper.toResponse
import es.uib.record.backend.model.InvitationResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class InvitationController(
    private val getInvitationsByUserIdUseCase: GetInvitationsByUserIdUseCase,
    private val acceptInvitationByIdUseCase: AcceptInvitationByIdUseCase,
    private val rejectInvitationByIdUseCase: RejectInvitationByIdUseCase
) : InvitationsApi {

    override fun acceptInvitation(invitationId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.acceptInvitationByIdUseCase.execute(invitationId, email)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    override fun listInvitations(): ResponseEntity<List<InvitationResponse>> {
        val email = SecurityContextHolder.getContext().authentication.name
        val invitations = this.getInvitationsByUserIdUseCase.execute(email)

        return ResponseEntity.ok(invitations.map { it.toResponse() })
    }

    override fun rejectInvitation(invitationId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.rejectInvitationByIdUseCase.execute(invitationId, email)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}