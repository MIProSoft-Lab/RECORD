CREATE TABLE group_publication_visibility
(
    group_id            UUID NOT NULL,
    owner_id            UUID NOT NULL,
    hidden_from_user_id UUID NOT NULL,
    CONSTRAINT group_publication_visibility_pkey PRIMARY KEY (group_id, owner_id, hidden_from_user_id)
);

ALTER TABLE group_publication_visibility
    ADD CONSTRAINT fk_gpv_group_id FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE;

CREATE INDEX idx_gpv_group_viewer ON group_publication_visibility (group_id, hidden_from_user_id);

CREATE INDEX idx_gpv_group_owner ON group_publication_visibility (group_id, owner_id);
