package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.dao.Faq;
import pl.ib.beauty.model.dto.AIDtoRequest;
import pl.ib.beauty.model.dto.AiTicketResponseDto;
import pl.ib.beauty.model.dto.PromptResponseDto;
import pl.ib.beauty.service.AIService;
import pl.ib.beauty.service.FaqService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/ai", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AIController {
    private final AIService aiService;
    private final FaqService faqService;

    @PostMapping
    PromptResponseDto query(@RequestBody AIDtoRequest request) {
        return new PromptResponseDto(aiService.query(request.prompt()));
    }

    @GetMapping
    AiTicketResponseDto query(@RequestParam String question) {
        List<Faq> faq = faqService.findByQuestion(question);
        return aiService.aiTicketResponse(question, faq);
    }
}
