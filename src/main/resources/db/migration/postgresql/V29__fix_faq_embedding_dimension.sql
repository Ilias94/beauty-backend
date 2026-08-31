DROP INDEX IF EXISTS beautypg.faq_embedding_idx;

ALTER TABLE beautypg.faq
    ALTER COLUMN embedding TYPE vector(384);

CREATE INDEX faq_embedding_idx
    ON beautypg.faq USING hnsw (embedding vector_cosine_ops);
