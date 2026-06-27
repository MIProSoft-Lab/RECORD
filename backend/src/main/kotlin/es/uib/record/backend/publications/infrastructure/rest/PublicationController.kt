package es.uib.record.backend.publications.infrastructure.rest

import es.uib.record.backend.api.PublicationsApi
import es.uib.record.backend.model.CreatePublicationRequest
import es.uib.record.backend.model.PublicationResponse
import es.uib.record.backend.model.PublicationSummaryResponse
import es.uib.record.backend.model.UpdatePublicationRequest
import es.uib.record.backend.publications.application.usecase.CreatePublicationUseCase
import es.uib.record.backend.publications.application.usecase.GetMyPublicationsUseCase
import es.uib.record.backend.publications.application.usecase.GetPublicationDetailUseCase
import es.uib.record.backend.publications.application.usecase.UpdatePublicationUseCase
import es.uib.record.backend.publications.infrastructure.mapper.toDto
import es.uib.record.backend.publications.infrastructure.mapper.toResponse
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class PublicationController(
    private val createPublicationUseCase: CreatePublicationUseCase,
    private val getMyPublicationsUseCase: GetMyPublicationsUseCase,
    private val getPublicationDetailUseCase: GetPublicationDetailUseCase,
    private val updatePublicationUseCase: UpdatePublicationUseCase,
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

    override fun listMyPublications(): ResponseEntity<List<PublicationSummaryResponse>> {
        val email = SecurityContextHolder.getContext().authentication.name
        val publications = this.getMyPublicationsUseCase.execute(email)

        return ResponseEntity.ok(publications.map { it.toResponse() })
    }

    override fun getPublicationDetail(
        publicationId: UUID
    ): ResponseEntity<PublicationResponse> {
        val publication = this.getPublicationDetailUseCase.execute(publicationId)

        return ResponseEntity.ok(publication.toResponse())
    }
}
