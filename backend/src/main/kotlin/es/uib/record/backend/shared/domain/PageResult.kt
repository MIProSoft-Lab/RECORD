package es.uib.record.backend.shared.domain

/**
 * Resultado paginado genérico, independiente de la tecnología de persistencia. Primer patrón de
 * paginación del proyecto; pensado para reutilizarse en futuras listas paginadas.
 */
data class PageResult<T>(
    val items: List<T>,
    val totalElements: Long,
    val page: Int,
    val size: Int,
) {
    val totalPages: Int = if (size <= 0) 0 else ((totalElements + size - 1) / size).toInt()
}
