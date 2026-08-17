CREATE SEQUENCE IF NOT EXISTS beautypg.question_id_seq
    START WITH 1
    INCREMENT BY 1;

ALTER TABLE beautypg.question
    ALTER COLUMN id SET DEFAULT nextval('beautypg.question_id_seq');

ALTER SEQUENCE beautypg.question_id_seq
    OWNED BY beautypg.question.id;

SELECT setval(
    'beautypg.question_id_seq',
    COALESCE((SELECT MAX(id) FROM beautypg.question), 1),
    true
);