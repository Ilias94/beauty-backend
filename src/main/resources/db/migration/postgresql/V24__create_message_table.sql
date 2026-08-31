CREATE TABLE beautypg.message (
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT NOT NULL REFERENCES beautypg.users(id),
    recipient_id BIGINT NOT NULL REFERENCES beautypg.users(id),
    content     TEXT NOT NULL,
    sent_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    read        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_message_sender    ON beautypg.message(sender_id);
CREATE INDEX idx_message_recipient ON beautypg.message(recipient_id);
CREATE INDEX idx_message_sent_at   ON beautypg.message(sent_at DESC);
