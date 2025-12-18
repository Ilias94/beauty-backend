CREATE TABLE video (
    id BIGSERIAL PRIMARY KEY,
    title varchar(255) NOT NULL,
    path varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    course_id bigint NOT NULL,
    CONSTRAINT fk_video_course FOREIGN KEY (course_id) REFERENCES course(id)
);
