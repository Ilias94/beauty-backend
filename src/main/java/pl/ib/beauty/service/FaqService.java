package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Faq;
import pl.ib.beauty.model.dto.FaqDtoRequest;
import pl.ib.beauty.repository.FaqRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaqService {

    private static final String ENTITY_NOT_FOUND_MESSAGE = "FAQ not found with id: ";

    private final FaqRepository faqRepository;
    private final EmbeddingModel embeddingModel;

    public List<Faq> getAll() {
        return faqRepository.findAll();
    }

    public Faq getById(UUID id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE + id));
    }

    @Transactional
    public Faq create(Faq faq) {
        faq.setEmbedding(generateEmbedding(faq.getQuestion()));
        return faqRepository.save(faq);
    }

    @Transactional
    public Faq update(UUID id, FaqDtoRequest request) {
        Faq existing = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE + id));
        existing.setQuestion(request.question());
        existing.setAnswer(request.answer());
        existing.setEmbedding(generateEmbedding(request.question()));
        return faqRepository.save(existing);
    }

    private float[] generateEmbedding(String question) {
        return embeddingModel.embed(question);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!faqRepository.existsById(id)) {
            throw new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE + id);
        }
        faqRepository.deleteById(id);
    }

    public List<Faq> findByQuestion(String question) {
        String vector = Arrays.toString(embeddingModel.embed(question));
        return faqRepository.findByEmbedding(vector, 0.3);
    }

    @Transactional
    public void reindexAll() {
        faqRepository.findAll().forEach(faq -> {
            faq.setEmbedding(generateEmbedding(faq.getQuestion()));
            faqRepository.save(faq);
        });
    }
}
