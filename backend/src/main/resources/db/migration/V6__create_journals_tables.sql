CREATE TABLE journals
(
    id                UUID                        NOT NULL,
    clarivate_id      VARCHAR(255)                NOT NULL,
    name              VARCHAR(512)                NOT NULL,
    issn              VARCHAR(32),
    e_issn            VARCHAR(32),
    publisher_name    VARCHAR(512),
    publisher_country VARCHAR(255),
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    last_synced_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT journals_pkey PRIMARY KEY (id)
);

CREATE TABLE categories
(
    id      UUID         NOT NULL,
    name    VARCHAR(512) NOT NULL,
    edition VARCHAR(32),
    CONSTRAINT categories_pkey PRIMARY KEY (id)
);

CREATE TABLE journal_metrics
(
    id            UUID                        NOT NULL,
    journal_id    UUID                        NOT NULL,
    report_year   INTEGER                     NOT NULL,
    impact_factor NUMERIC(10, 3),
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT journal_metrics_pkey PRIMARY KEY (id)
);

CREATE TABLE journal_category_quartiles
(
    id                UUID           NOT NULL,
    journal_metric_id UUID           NOT NULL,
    journal_id        UUID           NOT NULL,
    category_id       UUID           NOT NULL,
    report_year       INTEGER        NOT NULL,
    quartile          VARCHAR(2)     NOT NULL,
    impact_factor     NUMERIC(10, 3),
    CONSTRAINT journal_category_quartiles_pkey PRIMARY KEY (id)
);

CREATE TABLE journal_sync_state
(
    id                     UUID         NOT NULL,
    clarivate_last_updated VARCHAR(255),
    status                 VARCHAR(20)  NOT NULL,
    run_started_at         TIMESTAMP WITHOUT TIME ZONE,
    run_finished_at        TIMESTAMP WITHOUT TIME ZONE,
    processed_count        INTEGER      NOT NULL DEFAULT 0,
    total_count            INTEGER,
    failed_count           INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT journal_sync_state_pkey PRIMARY KEY (id)
);

-- Unique constraints
ALTER TABLE journals
    ADD CONSTRAINT uk_journals_clarivate_id UNIQUE (clarivate_id);

ALTER TABLE categories
    ADD CONSTRAINT uk_categories_name_edition UNIQUE (name, edition);

ALTER TABLE journal_metrics
    ADD CONSTRAINT uk_journal_metrics_journal_year UNIQUE (journal_id, report_year);

ALTER TABLE journal_category_quartiles
    ADD CONSTRAINT uk_journal_category_quartiles UNIQUE (journal_id, category_id, report_year);

-- Check constraints
ALTER TABLE journal_category_quartiles
    ADD CONSTRAINT chk_jcq_quartile CHECK (quartile IN ('Q1', 'Q2', 'Q3', 'Q4'));

-- Foreign keys
ALTER TABLE journal_metrics
    ADD CONSTRAINT fk_journal_metrics_journal_id FOREIGN KEY (journal_id) REFERENCES journals (id) ON DELETE CASCADE;

ALTER TABLE journal_category_quartiles
    ADD CONSTRAINT fk_jcq_metric_id FOREIGN KEY (journal_metric_id) REFERENCES journal_metrics (id) ON DELETE CASCADE;

ALTER TABLE journal_category_quartiles
    ADD CONSTRAINT fk_jcq_journal_id FOREIGN KEY (journal_id) REFERENCES journals (id) ON DELETE CASCADE;

ALTER TABLE journal_category_quartiles
    ADD CONSTRAINT fk_jcq_category_id FOREIGN KEY (category_id) REFERENCES categories (id);

-- Indexes for efficient future search (by category + year + quartile, impact factor, name)
CREATE INDEX idx_journal_metrics_journal_id ON journal_metrics (journal_id);
CREATE INDEX idx_journal_metrics_year ON journal_metrics (report_year);
CREATE INDEX idx_jcq_category_year_quartile ON journal_category_quartiles (category_id, report_year, quartile);
CREATE INDEX idx_jcq_year_quartile ON journal_category_quartiles (report_year, quartile);
CREATE INDEX idx_jcq_journal_id ON journal_category_quartiles (journal_id);
CREATE INDEX idx_journals_name ON journals (name);
CREATE INDEX idx_journals_publisher_name ON journals (publisher_name);

-- Seed the single sync-state row
INSERT INTO journal_sync_state (id, status, processed_count, failed_count)
VALUES ('00000000-0000-0000-0000-0000000000a1', 'IDLE', 0, 0);
