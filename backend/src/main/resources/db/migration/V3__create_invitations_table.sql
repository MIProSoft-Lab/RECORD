CREATE TABLE invitations
(
    id              UUID                        NOT NULL,
    group_id        UUID                        NOT NULL,
    invitee_user_id UUID                        NOT NULL,
    inviter_user_id UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT invitations_pkey PRIMARY KEY (id),
    CONSTRAINT fk_invitations_group_id FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT uk_invitations_group_invitee UNIQUE (group_id, invitee_user_id),
    CONSTRAINT chk_invitations_not_self CHECK (inviter_user_id <> invitee_user_id)
);

CREATE INDEX idx_invitations_invitee_user_id ON invitations (invitee_user_id);