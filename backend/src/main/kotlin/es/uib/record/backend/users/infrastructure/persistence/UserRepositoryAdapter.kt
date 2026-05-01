package es.uib.record.backend.users.infrastructure.persistence

import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.domain.UserRepository
import es.uib.record.backend.users.infrastructure.mapper.toDomain
import es.uib.record.backend.users.infrastructure.mapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val springDataJpaUserRepository: SpringDataJpaUserRepository
) : UserRepository {

    override fun save(user: User): User {
        val userEntity = this.springDataJpaUserRepository.save(user.toEntity())
        return userEntity.toDomain()
    }

    override fun findByEmail(email: String): User? {
        return this.springDataJpaUserRepository.findByEmail(email)?.toDomain()
    }
}