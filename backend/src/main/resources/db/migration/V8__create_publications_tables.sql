CREATE TABLE publications
(
    id         UUID                        NOT NULL,
    title      VARCHAR(512)                NOT NULL,
    abstract   TEXT,
    doi        VARCHAR(255),
    journal_id UUID                        NOT NULL,
    group_id   UUID                        NOT NULL,
    status     VARCHAR(50)                 NOT NULL DEFAULT 'PLANNED',
    created_by UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT publications_pkey PRIMARY KEY (id),
    CONSTRAINT chk_publications_status CHECK (status IN
                                              ('PLANNED', 'SUBMITTED', 'UNDER_REVIEW', 'MINOR_REVISION',
                                               'MAJOR_REVISION', 'REJECTED', 'ACCEPTED', 'PUBLISHED')),
    CONSTRAINT fk_publications_journal_id FOREIGN KEY (journal_id) REFERENCES journals (id),
    CONSTRAINT fk_publications_group_id FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE TABLE publication_authors
(
    publication_id UUID NOT NULL,
    user_id        UUID NOT NULL,
    CONSTRAINT publication_authors_pkey PRIMARY KEY (publication_id, user_id),
    CONSTRAINT fk_publication_authors_publication_id FOREIGN KEY (publication_id) REFERENCES publications (id) ON DELETE CASCADE
);

CREATE INDEX idx_publications_created_by ON publications (created_by);
