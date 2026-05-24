package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import org.springframework.stereotype.Component

@Component
class SearchUserUseCase(private val userRepository: UserRepository) {
    fun execute(query: String): List<User> {
        return this.userRepository.searchByEmailOrName(query)
    }
}
