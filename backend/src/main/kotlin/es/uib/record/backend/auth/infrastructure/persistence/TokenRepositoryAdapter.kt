package es.uib.record.backend.auth.infrastructure.persistence

import es.uib.record.backend.auth.domain.Token
import es.uib.record.backend.auth.domain.TokenRepository
import es.uib.record.backend.auth.infrastructure.mapper.toDomain
import es.uib.record.backend.auth.infrastructure.mapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class TokenRepositoryAdapter(
    private val springDataJpaTokenRepository: SpringDataJpaTokenRepository
) : TokenRepository {

    override fun save(token: Token) {
        this.springDataJpaTokenRepository.save(token.toEntity())
    }

    override fun findByToken(token: String): Token? {
        val tokenEntity = this.springDataJpaTokenRepository.findByToken(token)
        return tokenEntity?.toDomain()
    }
}