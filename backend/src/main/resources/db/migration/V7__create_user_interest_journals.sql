CREATE TABLE user_interest_journals
(
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    journal_id UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT user_interest_journals_pkey PRIMARY KEY (id)
);

-- A user can mark a journal as interest only once
ALTER TABLE user_interest_journals
    ADD CONSTRAINT uk_user_interest_journals_user_journal UNIQUE (user_id, journal_id);

-- Foreign keys
ALTER TABLE user_interest_journals
    ADD CONSTRAINT fk_user_interest_journals_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE user_interest_journals
    ADD CONSTRAINT fk_user_interest_journals_journal_id FOREIGN KEY (journal_id) REFERENCES journals (id) ON DELETE CASCADE;

-- Index for listing/filtering a user's interest journals
CREATE INDEX idx_user_interest_journals_user_id ON user_interest_journals (user_id);
