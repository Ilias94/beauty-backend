-- How many people rated a course, kept next to its average so lists can show "4.6 (12)".
ALTER TABLE beautypg.course ADD COLUMN rating_count integer NOT NULL DEFAULT 0;

UPDATE beautypg.course c
SET rating_count = s.cnt,
    rating = s.avg
FROM (SELECT course_id, COUNT(*) AS cnt, AVG(value) AS avg
      FROM beautypg.rating
      GROUP BY course_id) s
WHERE s.course_id = c.id;
