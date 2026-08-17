package pl.ib.beauty.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingModelConfig {
    @Primary
    @Bean
    public EmbeddingModel embeddingModelMultilingualMiniLM() {
        TransformersEmbeddingModel transformersEmbeddingModel = new TransformersEmbeddingModel();
        transformersEmbeddingModel.setTokenizerResource("classpath:tokenizer.json");
        transformersEmbeddingModel.setModelResource("classpath:model.onnx");
        transformersEmbeddingModel.setModelOutputName("last_hidden_state");
        return transformersEmbeddingModel;
    }
}
