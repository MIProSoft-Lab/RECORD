package es.uib.record.backend.users.open

import java.util.UUID

interface UserFacade {
    fun createUser(email: String, firstName: String, lastName: String, password: String): UUID
    fun getUserIdByEmail(email: String): UUID
}