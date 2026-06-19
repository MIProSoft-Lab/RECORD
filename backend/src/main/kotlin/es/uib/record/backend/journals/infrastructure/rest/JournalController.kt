package es.uib.record.backend.journals.infrastructure.rest

import es.uib.record.backend.api.JournalsApi
import es.uib.record.backend.journals.application.usecase.GetJournalDetailUseCase
import es.uib.record.backend.journals.application.usecase.ListInterestJournalsUseCase
import es.uib.record.backend.journals.application.usecase.ListJournalCategoriesUseCase
import es.uib.record.backend.journals.application.usecase.MarkJournalInterestUseCase
import es.uib.record.backend.journals.application.usecase.SearchJournalsUseCase
import es.uib.record.backend.journals.application.usecase.TriggerJournalSyncUseCase
import es.uib.record.backend.journals.application.usecase.UnmarkJournalInterestUseCase
import es.uib.record.backend.journals.infrastructure.mapper.toResponse
import es.uib.record.backend.model.CategoryResponse
import es.uib.record.backend.model.JournalDetailResponse
import es.uib.record.backend.model.JournalSearchPageResponse
import es.uib.record.backend.model.JournalSyncStatusResponse
import es.uib.record.backend.model.Quartile
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import es.uib.record.backend.journals.domain.model.Quartile as DomainQuartile

/**
 * Único controller del módulo de journals: implementa la interfaz generada [JournalsApi]. Debe ser
 * uno solo, ya que los métodos por defecto de la interfaz registran un mapping por endpoint y dos
 * controllers que la implementen producirían mappings ambiguos.
 */
@RestController
class JournalController(
    private val triggerJournalSyncUseCase: TriggerJournalSyncUseCase,
    private val searchJournalsUseCase: SearchJournalsUseCase,
    private val listJournalCategoriesUseCase: ListJournalCategoriesUseCase,
    private val getJournalDetailUseCase: GetJournalDetailUseCase,
    private val listInterestJournalsUseCase: ListInterestJournalsUseCase,
    private val markJournalInterestUseCase: MarkJournalInterestUseCase,
    private val unmarkJournalInterestUseCase: UnmarkJournalInterestUseCase,
) : JournalsApi {

    override fun triggerJournalSync(
        xSyncToken: String?
    ): ResponseEntity<JournalSyncStatusResponse> {
        val state = this.triggerJournalSyncUseCase.execute(xSyncToken)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(state.toResponse())
    }

    override fun searchJournals(
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): ResponseEntity<JournalSearchPageResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val result =
            this.searchJournalsUseCase.execute(
                email,
                name,
                categoryId,
                quartile?.let { DomainQuartile.valueOf(it.name) },
                page,
                size,
            )
        return ResponseEntity.ok(result.toResponse())
    }

    override fun listJournalCategories(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(this.listJournalCategoriesUseCase.execute().map { it.toResponse() })
    }

    override fun getJournalDetail(journalId: UUID): ResponseEntity<JournalDetailResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        return ResponseEntity.ok(this.getJournalDetailUseCase.execute(email, journalId).toResponse())
    }

    override fun listInterestJournals(
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): ResponseEntity<JournalSearchPageResponse> {
        val email = SecurityContextHolder.getContext().authentication.name
        val result =
            this.listInterestJournalsUseCase.execute(
                email,
                name,
                categoryId,
                quartile?.let { DomainQuartile.valueOf(it.name) },
                page,
                size,
            )
        return ResponseEntity.ok(result.toResponse())
    }

    override fun markJournalAsInterest(journalId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.markJournalInterestUseCase.execute(email, journalId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    override fun unmarkJournalAsInterest(journalId: UUID): ResponseEntity<Unit> {
        val email = SecurityContextHolder.getContext().authentication.name
        this.unmarkJournalInterestUseCase.execute(email, journalId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
