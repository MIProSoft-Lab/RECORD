package es.uib.record.backend.publications.infrastructure.persistence.repository

import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationAuthorEntity
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationEntity
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationStatusHistoryEntity
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpringDataJpaPublicationRepositorySearchTest
@Autowired
constructor(
    private val repository: SpringDataJpaPublicationRepository,
    private val entityManager: TestEntityManager,
) {

    private companion object {
        val USER = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        val OTHER = UUID.fromString("00000000-0000-0000-0000-0000000000a2")
        val JOURNAL_A = UUID.fromString("00000000-0000-0000-0000-0000000000b1")
        val JOURNAL_B = UUID.fromString("00000000-0000-0000-0000-0000000000b2")
        val GROUP = UUID.fromString("00000000-0000-0000-0000-0000000000c1")
        val PAGE = PageRequest.of(0, 20)
        val NO_STALE_FILTER: Instant = Instant.parse("9999-01-01T00:00:00Z")
    }

    // Las FKs publications.journal_id → journals y publications.group_id → groups obligan a sembrar
    // las filas padre mínimas antes de insertar publicaciones.
    @BeforeEach
    fun seedReferencedRows() {
        val now = Timestamp.from(Instant.now())
        insertJournal(JOURNAL_A, "Journal A", now)
        insertJournal(JOURNAL_B, "Journal B", now)
        entityManager.entityManager
            .createNativeQuery(
                "INSERT INTO groups (id, created_at, name, created_by) VALUES (?, ?, ?, ?)"
            )
            .setParameter(1, GROUP)
            .setParameter(2, now)
            .setParameter(3, "Group")
            .setParameter(4, USER)
            .executeUpdate()
    }

    private fun insertJournal(id: UUID, name: String, createdAt: Timestamp) {
        entityManager.entityManager
            .createNativeQuery(
                "INSERT INTO journals (id, clarivate_id, name, created_at) VALUES (?, ?, ?, ?)"
            )
            .setParameter(1, id)
            .setParameter(2, id.toString())
            .setParameter(3, name)
            .setParameter(4, createdAt)
            .executeUpdate()
    }

    /** Persiste una publicación con sus autores (userId → posición) y un único cambio de estado. */
    private fun persist(
        title: String,
        status: PublicationStatus,
        journalId: UUID,
        authors: List<Pair<UUID?, Int>>,
        lastChangedAt: Instant = Instant.now(),
    ): UUID {
        val entity =
            PublicationEntity(
                title = title,
                journalId = journalId,
                groupId = GROUP,
                status = status,
                createdBy = authors.firstOrNull { it.first != null }?.first ?: USER,
                authors =
                    authors
                        .map { (userId, position) ->
                            PublicationAuthorEntity(userId = userId, position = position)
                        }
                        .toMutableList(),
                statusHistory =
                    mutableListOf(
                        PublicationStatusHistoryEntity(
                            status = status,
                            journalId = journalId,
                            changedAt = lastChangedAt,
                            position = 0,
                        )
                    ),
            )
        return repository.saveAndFlush(entity).id!!
    }

    private fun search(
        title: String? = null,
        status: PublicationStatus? = null,
        journalId: UUID? = null,
        staleBefore: Instant? = null,
        excludeFinalStatuses: Boolean = false,
        onlyMainAuthor: Boolean = false,
        userId: UUID = USER,
    ) =
        repository.searchPublicationIds(
            userId = userId,
            titlePattern = title?.let { "%${it.lowercase()}%" },
            status = status,
            journalId = journalId,
            staleBefore = staleBefore ?: NO_STALE_FILTER,
            excludeFinalStatuses = excludeFinalStatuses,
            onlyMainAuthor = onlyMainAuthor,
            pageable = PAGE,
        )

    @Test
    fun `returns only publications where the user is an author`() {
        val mine = persist("Mine", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0))
        persist("Theirs", PublicationStatus.PLANNED, JOURNAL_A, listOf(OTHER to 0))

        val result = search()

        assertEquals(listOf(mine), result.content)
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `filters by title case-insensitively`() {
        val match =
            persist("Deep Learning", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0))
        persist("Quantum", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0))

        assertEquals(listOf(match), search(title = "deep").content)
    }

    @Test
    fun `filters by status and by journal`() {
        val submittedA = persist("A", PublicationStatus.SUBMITTED, JOURNAL_A, listOf(USER to 0))
        persist("B", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0))
        persist("C", PublicationStatus.SUBMITTED, JOURNAL_B, listOf(USER to 0))

        assertEquals(
            setOf(submittedA),
            search(status = PublicationStatus.SUBMITTED, journalId = JOURNAL_A).content.toSet(),
        )
    }

    @Test
    fun `onlyMainAuthor keeps only publications where the user is the first author`() {
        val asMain =
            persist("Main", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0, OTHER to 1))
        persist("CoAuthor", PublicationStatus.PLANNED, JOURNAL_A, listOf(OTHER to 0, USER to 1))

        assertEquals(listOf(asMain), search(onlyMainAuthor = true).content)
        // Sin el filtro, aparecen ambas (es autor en las dos).
        assertEquals(2, search().totalElements)
    }

    @Test
    fun `stale filter keeps publications older than the threshold and excludes final statuses`() {
        val threshold = Instant.now().minus(Duration.ofDays(30))
        val stale =
            persist(
                "Stuck",
                PublicationStatus.UNDER_REVIEW,
                JOURNAL_A,
                listOf(USER to 0),
                lastChangedAt = Instant.now().minus(Duration.ofDays(60)),
            )
        // Reciente: no estancada.
        persist(
            "Recent",
            PublicationStatus.UNDER_REVIEW,
            JOURNAL_A,
            listOf(USER to 0),
            lastChangedAt = Instant.now().minus(Duration.ofDays(5)),
        )
        // Antigua pero en estado final: debe excluirse al activar el filtro.
        persist(
            "PublishedOld",
            PublicationStatus.PUBLISHED,
            JOURNAL_A,
            listOf(USER to 0),
            lastChangedAt = Instant.now().minus(Duration.ofDays(90)),
        )

        val result = search(staleBefore = threshold, excludeFinalStatuses = true)

        assertEquals(listOf(stale), result.content)
    }

    @Test
    fun `paginates results and reports total`() {
        repeat(3) { persist("P$it", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0)) }

        val firstPage =
            repository.searchPublicationIds(
                userId = USER,
                titlePattern = null,
                status = null,
                journalId = null,
                staleBefore = NO_STALE_FILTER,
                excludeFinalStatuses = false,
                onlyMainAuthor = false,
                pageable = PageRequest.of(0, 2),
            )

        assertEquals(2, firstPage.content.size)
        assertEquals(3, firstPage.totalElements)
        assertTrue(firstPage.totalPages == 2)
    }

    @Test
    fun `group search returns every publication of the group regardless of author`() {
        val mine = persist("Mine", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0))
        val theirs = persist("Theirs", PublicationStatus.PLANNED, JOURNAL_A, listOf(OTHER to 0))

        val result =
            repository.searchGroupPublicationIds(
                groupId = GROUP,
                titlePattern = null,
                status = null,
                journalId = null,
                staleBefore = NO_STALE_FILTER,
                excludeFinalStatuses = false,
                pageable = PAGE,
            )

        assertEquals(setOf(mine, theirs), result.content.toSet())
        assertEquals(2, result.totalElements)
    }

    @Test
    fun `group search by authors keeps only publications authored by the given members, deduplicated`() {
        // Publicación co-autorada por ambos: debe aparecer una sola vez aunque ambos se filtren.
        val shared =
            persist("Shared", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0, OTHER to 1))
        // Publicación solo de OTHER: excluida al filtrar por USER.
        persist("OnlyOther", PublicationStatus.PLANNED, JOURNAL_A, listOf(OTHER to 0))
        // Publicación solo de USER.
        val onlyMine = persist("OnlyMine", PublicationStatus.PLANNED, JOURNAL_A, listOf(USER to 0))

        val filteredByUser =
            repository.searchGroupPublicationIdsByAuthors(
                groupId = GROUP,
                authorIds = listOf(USER),
                titlePattern = null,
                status = null,
                journalId = null,
                staleBefore = NO_STALE_FILTER,
                excludeFinalStatuses = false,
                pageable = PAGE,
            )
        assertEquals(setOf(shared, onlyMine), filteredByUser.content.toSet())

        // Filtrar por ambos no duplica la publicación compartida.
        val filteredByBoth =
            repository.searchGroupPublicationIdsByAuthors(
                groupId = GROUP,
                authorIds = listOf(USER, OTHER),
                titlePattern = null,
                status = null,
                journalId = null,
                staleBefore = NO_STALE_FILTER,
                excludeFinalStatuses = false,
                pageable = PAGE,
            )
        assertEquals(3, filteredByBoth.totalElements)
        assertEquals(setOf(shared, onlyMine).size + 1, filteredByBoth.content.toSet().size)
    }
}
