package es.uib.record.backend.publications.infrastructure.rest

import es.uib.record.backend.api.PublicationsApi
import es.uib.record.backend.model.ChangePublicationStatusRequest
import es.uib.record.backend.model.CreatePublicationRequest
import es.uib.record.backend.model.GroupPublicationListPageResponse
import es.uib.record.backend.model.PublicationListPageResponse
import es.uib.record.backend.model.PublicationResponse
import es.uib.record.backend.model.PublicationStatus as ApiPublicationStatus
import es.uib.record.backend.model.ResubmitPublicationRequest
import es.uib.record.backend.model.UpdatePublicationRequest
import es.uib.record.backend.publications.application.usecase.ChangePublicationStatusUseCase
import es.uib.record.backend.publications.application.usecase.CreatePublicationUseCase
import es.uib.record.backend.publications.application.usecase.DeletePublicationUseCase
import es.uib.record.backend.publications.application.usecase.GetPublicationDetailUseCase
import es.uib.record.backend.publications.application.usecase.ResubmitPublicationUseCase
import es.uib.record.backend.publications.application.usecase.SearchGroupPublicationsUseCase
import es.uib.record.backend.publications.application.usecase.SearchMyPublicationsUseCase
import es.uib.record.backend.publications.application.usecase.UpdatePublicationUseCase
import es.uib.record.backend.publications.infrastructure.mapper.toDomain
import es.uib.record.backend.publications.infrastructure.mapper.toDto
import es.uib.record.backend.publications.infrastructure.mapper.toGroupResponse
import es.uib.record.backend.publications.infrastructure.mapper.toResponse
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class PublicationController(
    private val createPublicationUseCase: CreatePublicationUseCase,
    private val searchMyPublicationsUseCase: SearchMyPublicationsUseCase,
    private val searchGroupPublicationsUseCase: SearchGroupPublicationsUseCase,
    private val getPublicationDetailUseCase: GetPublicationDetailUseCase,
    private val updatePublicationUseCase: UpdatePublicationUseCase,
    private val changePublicationStatusUseCase: ChangePublicationStatusUseCase,
    private val resubmitPublicationUseCase: ResubmitPublicationUseCase,
    private val deletePublicationUseCase: DeletePublicationUseCase,
) : PublicationsApi {

    override fun createPublication(
        createPublicationRequest: CreatePublicationRequest
    ): ResponseEntity<PublicationResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val createdPublication =
            this.createPublicationUseCase.execute(createPublicationRequest.toDto(), email)

        return ResponseEntity.ok(createdPublication.toResponse())
    }

    override fun updatePublication(
        publicationId: UUID,
        updatePublicationRequest: UpdatePublicationRequest,
    ): ResponseEntity<PublicationResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val updatedPublication =
            this.updatePublicationUseCase.execute(
                publicationId,
                updatePublicationRequest.toDto(),
                email,
            )

        return ResponseEntity.ok(updatedPublication.toResponse())
    }

    override fun changePublicationStatus(
        publicationId: UUID,
        changePublicationStatusRequest: ChangePublicationStatusRequest,
    ): ResponseEntity<PublicationResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val updatedPublication =
            this.changePublicationStatusUseCase.execute(
                publicationId,
                changePublicationStatusRequest.status.toDomain(),
                email,
                changePublicationStatusRequest.comment,
            )

        return ResponseEntity.ok(updatedPublication.toResponse())
    }

    override fun resubmitPublication(
        publicationId: UUID,
        resubmitPublicationRequest: ResubmitPublicationRequest,
    ): ResponseEntity<PublicationResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val updatedPublication =
            this.resubmitPublicationUseCase.execute(
                publicationId,
                resubmitPublicationRequest.journalId,
                email,
                resubmitPublicationRequest.comment,
            )

        return ResponseEntity.ok(updatedPublication.toResponse())
    }

    override fun listMyPublications(
        title: String?,
        journalId: UUID?,
        status: ApiPublicationStatus?,
        minDaysInStatus: Int?,
        onlyAsMainAuthor: Boolean,
        page: Int,
        size: Int,
    ): ResponseEntity<PublicationListPageResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val result =
            this.searchMyPublicationsUseCase.execute(
                email = email,
                title = title,
                journalId = journalId,
                status = status?.toDomain(),
                minDaysInStatus = minDaysInStatus,
                onlyAsMainAuthor = onlyAsMainAuthor,
                page = page,
                size = size,
            )

        return ResponseEntity.ok(result.toResponse())
    }

    override fun listGroupPublications(
        groupId: UUID,
        memberIds: List<UUID>?,
        title: String?,
        journalId: UUID?,
        status: ApiPublicationStatus?,
        minDaysInStatus: Int?,
        page: Int,
        size: Int,
    ): ResponseEntity<GroupPublicationListPageResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val result =
            this.searchGroupPublicationsUseCase.execute(
                email = email,
                groupId = groupId,
                memberIds = memberIds,
                title = title,
                journalId = journalId,
                status = status?.toDomain(),
                minDaysInStatus = minDaysInStatus,
                page = page,
                size = size,
            )

        return ResponseEntity.ok(result.toGroupResponse())
    }

    override fun getPublicationDetail(publicationId: UUID): ResponseEntity<PublicationResponse> {
        val publication = this.getPublicationDetailUseCase.execute(publicationId)

        return ResponseEntity.ok(publication.toResponse())
    }

    override fun deletePublication(publicationId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.deletePublicationUseCase.execute(publicationId, email)

        return ResponseEntity.noContent().build()
    }
}
