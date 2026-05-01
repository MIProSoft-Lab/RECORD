package es.uib.record.backend.users.domain

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
}