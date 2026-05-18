package es.uib.record.backend.users.application.usecase

import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetAllUsersByIdsUseCase(
    private val userRepository: UserRepository
) {
    fun execute(userIds: List<UUID>): List<User> {
        return userRepository.findAllByIds(userIds)
    }
}