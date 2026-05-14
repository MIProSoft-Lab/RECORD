CREATE TABLE token
(
    id      UUID NOT NULL,
    revoked BOOLEAN  NOT NULL,
    token   VARCHAR(255),
    user_id UUID NOT NULL,
    CONSTRAINT token_pkey PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                 UUID                        NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    email              VARCHAR(255)                NOT NULL,
    first_name         VARCHAR(255)                NOT NULL,
    last_name          VARCHAR(255)                NOT NULL,
    password           VARCHAR(255)                NOT NULL,
    push_notifications BOOLEAN                     NOT NULL,
    profile_image_url  VARCHAR(255),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE token
    ADD CONSTRAINT ukpddrhgwxnms2aceeku9s2ewy5 UNIQUE (token);