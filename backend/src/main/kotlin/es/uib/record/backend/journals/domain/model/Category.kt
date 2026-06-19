package es.uib.record.backend.journals.domain.model

import java.util.UUID

data class Category(val id: UUID? = null, val name: String, val edition: String? = null)
