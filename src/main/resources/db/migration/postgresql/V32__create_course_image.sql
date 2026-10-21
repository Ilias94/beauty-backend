-- A course's photo gallery. The image at position 0 is the cover; course.image_url mirrors it so
-- course lists keep reading a single column.
CREATE TABLE beautypg.course_image (
    id BIGSERIAL PRIMARY KEY,
    course_id bigint NOT NULL REFERENCES beautypg.course(id) ON DELETE CASCADE,
    file_name varchar(255) NOT NULL,
    position integer NOT NULL,
    created_date timestamp(6) NOT NULL DEFAULT now()
);

CREATE INDEX idx_course_image_course_position ON beautypg.course_image (course_id, position);
