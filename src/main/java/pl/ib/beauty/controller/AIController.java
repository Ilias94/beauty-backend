package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ib.beauty.model.dto.AIDtoRequest;
import pl.ib.beauty.model.dto.PromptResponseDto;
import pl.ib.beauty.service.AIService;

@RestController
@RequestMapping(value = "/api/v1/ai", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AIController {
    private final AIService aiService;

    @PostMapping
    PromptResponseDto query(@RequestBody AIDtoRequest request) {
        return new PromptResponseDto(aiService.query(request.prompt()));
    }
}
