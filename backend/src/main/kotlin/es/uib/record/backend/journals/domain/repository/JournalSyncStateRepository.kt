package es.uib.record.backend.journals.domain.repository

import es.uib.record.backend.journals.domain.model.SyncState

interface JournalSyncStateRepository {
    /** Devuelve la fila única de estado de sincronización (siempre existe, sembrada por Flyway). */
    fun get(): SyncState

    fun save(syncState: SyncState): SyncState
}
