CREATE TABLE beautypg.course_message (
    id        BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES beautypg.course(id),
    sender_id BIGINT NOT NULL REFERENCES beautypg.users(id),
    content   TEXT NOT NULL,
    sent_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_course_message_course_id ON beautypg.course_message(course_id, sent_at);
