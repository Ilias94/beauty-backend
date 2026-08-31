CREATE TABLE beautypg.ticket (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    type           VARCHAR(50)  NOT NULL,
    status         VARCHAR(50)  NOT NULL DEFAULT 'OPEN',
    author_id      BIGINT       NOT NULL REFERENCES beautypg.users (id),
    assigned_to_id BIGINT                REFERENCES beautypg.users (id),
    replies        JSONB        NOT NULL DEFAULT '[]',
    resolution     JSONB,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);
