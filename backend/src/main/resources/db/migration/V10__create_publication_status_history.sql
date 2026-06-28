-- Historial de estados de una publicación: registra cada transición con su estado,
-- el journal asociado en ese momento (refleja el journal anterior tras un reenvío),
-- la fecha de la transición y un comentario opcional. position preserva el orden
-- cronológico. No se hace backfill: no hay datos reales todavía.

CREATE TABLE publication_status_history
(
    id             UUID                        NOT NULL,
    publication_id UUID                        NOT NULL,
    status         VARCHAR(50)                 NOT NULL,
    journal_id     UUID                        NOT NULL,
    changed_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    comment        TEXT,
    position       INTEGER                     NOT NULL DEFAULT 0,
    CONSTRAINT publication_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT chk_publication_status_history_status CHECK (status IN
                                              ('PLANNED', 'SUBMITTED', 'UNDER_REVIEW', 'MINOR_REVISION',
                                               'MAJOR_REVISION', 'REJECTED', 'ACCEPTED', 'PUBLISHED')),
    CONSTRAINT fk_publication_status_history_publication_id FOREIGN KEY (publication_id) REFERENCES publications (id) ON DELETE CASCADE
);

CREATE INDEX idx_publication_status_history_publication_id ON publication_status_history (publication_id);
