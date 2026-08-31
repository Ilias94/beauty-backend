CREATE TABLE beautypg.faq (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question    TEXT NOT NULL,
    answer      TEXT NOT NULL,
    embedding   vector(1536),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX faq_embedding_idx
    ON beautypg.faq USING hnsw (embedding vector_cosine_ops);
