CREATE TABLE groups
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        VARCHAR(255)                NOT NULL,
    description TEXT,
    created_by  UUID                        NOT NULL,
    CONSTRAINT groups_pkey PRIMARY KEY (id)
);

CREATE TABLE group_members
(
    group_id UUID         NOT NULL,
    user_id  UUID         NOT NULL,
    role     VARCHAR(50)  NOT NULL,
    CONSTRAINT group_members_pkey PRIMARY KEY (group_id, user_id),
    CONSTRAINT chk_group_members_role CHECK (role IN ('ADMIN', 'MEMBER'))
);

ALTER TABLE groups
    ADD CONSTRAINT uk_groups_name UNIQUE (name);

ALTER TABLE group_members
    ADD CONSTRAINT fk_group_members_group_id FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE;