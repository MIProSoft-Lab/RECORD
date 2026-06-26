package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class SearchUserUseCaseTest {

    companion object {
        private const val QUERY = "ana"
    }

    @Mock private lateinit var userRepository: UserRepository

    @InjectMocks private lateinit var searchUserUseCase: SearchUserUseCase

    @Test
    fun `should return the users matching the query by name or email`() {
        // Given
        val users = listOf(user("Ana", "García"), user("Juan", "Pérez"))
        given(userRepository.searchByEmailOrName(QUERY)).willReturn(users)

        // When
        val result = searchUserUseCase.execute(QUERY)

        // Then
        assertEquals(users, result)
    }

    @Test
    fun `should return an empty list when there are no matches`() {
        // Given
        given(userRepository.searchByEmailOrName(QUERY)).willReturn(emptyList())

        // When
        val result = searchUserUseCase.execute(QUERY)

        // Then
        assertTrue(result.isEmpty())
    }

    private fun user(firstName: String, lastName: String) =
        User(
            id = UUID.randomUUID(),
            email = "${firstName.lowercase()}@test.com",
            password = "hashed",
            firstName = firstName,
            lastName = lastName,
        )
}
