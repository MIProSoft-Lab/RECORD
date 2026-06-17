package es.uib.record.backend.auth.domain

import java.util.UUID

interface TokenRepository {
    fun save(token: Token)

    fun findByToken(token: String): Token?

    fun revokeAllByUserId(userId: UUID)
}
