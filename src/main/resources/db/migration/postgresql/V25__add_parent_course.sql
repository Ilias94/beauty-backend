ALTER TABLE beautypg.course ADD COLUMN parent_course_id BIGINT REFERENCES beautypg.course(id);
