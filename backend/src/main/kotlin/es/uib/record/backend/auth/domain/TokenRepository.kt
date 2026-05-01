package es.uib.record.backend.auth.domain

interface TokenRepository {
    fun save(token: Token)
}
