-- A private message can carry the course it is about ("Pytanie o kurs: ..."), e.g. a question
-- sent from a course page. Deleting the course keeps the message and drops the reference.
ALTER TABLE beautypg.message
    ADD COLUMN course_id BIGINT REFERENCES beautypg.course(id) ON DELETE SET NULL;
