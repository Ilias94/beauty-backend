CREATE TABLE beautypg.question (
    id BIGSERIAL PRIMARY KEY,

    course_id BIGINT NOT NULL,

    priority INTEGER NOT NULL,

    question_detail JSONB NOT NULL,

    CONSTRAINT fk_question_course
        FOREIGN KEY (course_id)
        REFERENCES beautypg.course(id)
        ON DELETE CASCADE
);
