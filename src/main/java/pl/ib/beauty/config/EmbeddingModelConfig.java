package pl.ib.beauty.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingModelConfig {
    @Primary
    @Bean
    public EmbeddingModel embeddingModelMultilingualMiniLM(@Value("${embedding.modelUri}") String modelUri) {
        TransformersEmbeddingModel transformersEmbeddingModel = new TransformersEmbeddingModel();
        transformersEmbeddingModel.setTokenizerResource("classpath:tokenizer.json");
        // The 470 MB model is not in git; it is downloaded once per container and cached in the temp dir.
        transformersEmbeddingModel.setModelResource(modelUri);
        transformersEmbeddingModel.setModelOutputName("last_hidden_state");
        return transformersEmbeddingModel;
    }
}
