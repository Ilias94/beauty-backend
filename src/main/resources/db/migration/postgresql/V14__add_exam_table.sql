CREATE TABLE beautypg.exam (
    id BIGINT NOT NULL,
    user_id BIGINT,
    course_id BIGINT,
    answers JSONB,
    CONSTRAINT exam_pkey PRIMARY KEY (id),
    CONSTRAINT exam_user_fk FOREIGN KEY (user_id)
        REFERENCES beautypg."users"(id),
    CONSTRAINT exam_course_fk FOREIGN KEY (course_id)
        REFERENCES beautypg.course(id)
);
