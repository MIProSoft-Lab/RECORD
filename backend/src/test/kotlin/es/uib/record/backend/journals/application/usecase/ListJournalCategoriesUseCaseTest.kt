package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.Category
import es.uib.record.backend.journals.domain.repository.CategoryRepository
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class ListJournalCategoriesUseCaseTest {

    @Mock private lateinit var categoryRepository: CategoryRepository

    private fun useCase() = ListJournalCategoriesUseCase(categoryRepository)

    @Test
    fun `returns the categories provided by the repository preserving order`() {
        val categories =
            listOf(
                Category(id = UUID.randomUUID(), name = "AGRICULTURE", edition = "SCIE"),
                Category(id = UUID.randomUUID(), name = "ONCOLOGY", edition = "SCIE"),
            )
        given(categoryRepository.findAllOrderedByName()).willReturn(categories)

        val result = useCase().execute()

        assertEquals(categories, result)
        assertEquals("AGRICULTURE", result.first().name)
    }
}
