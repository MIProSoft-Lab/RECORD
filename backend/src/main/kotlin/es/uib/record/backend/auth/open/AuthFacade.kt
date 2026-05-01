package es.uib.record.backend.auth.open

interface AuthFacade {
    fun isTokenActive(token: String): Boolean
}