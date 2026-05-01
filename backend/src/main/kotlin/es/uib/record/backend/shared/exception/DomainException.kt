package es.uib.record.backend.shared.exception

abstract class DomainException(
    override val message: String,
    val code: String,
    val type: ErrorType,
    val params: Map<String, Any> = emptyMap()
) : RuntimeException(message)