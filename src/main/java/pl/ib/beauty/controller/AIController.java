package pl.ib.beauty.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
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
// Each call costs an Anthropic request: signed-in users only.
@PreAuthorize("isAuthenticated()")
public class AIController {
    private final AIService aiService;
    private final FaqService faqService;

    // Explicit operation ids keep the generated frontend client stable: without them springdoc
    // numbers same-named methods (query, query1) in an order that can change between versions.
    @PostMapping
    @Operation(operationId = "query")
    public PromptResponseDto query(@RequestBody AIDtoRequest request) {
        return new PromptResponseDto(aiService.query(request.prompt()));
    }

    @GetMapping
    @Operation(operationId = "answerTicketQuestion")
    public AiTicketResponseDto query(@RequestParam String question) {
        List<Faq> faq = faqService.findByQuestion(question);
        return aiService.aiTicketResponse(question, faq);
    }
}
