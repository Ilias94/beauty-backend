package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ib.beauty.model.dto.AssistantDtoRequest;
import pl.ib.beauty.model.dto.AssistantDtoResponse;
import pl.ib.beauty.service.AssistantService;

@RestController
@RequestMapping(value = "/api/v1/assistant", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public AssistantDtoResponse ask(@RequestBody @Valid AssistantDtoRequest request) {
        return assistantService.ask(request);
    }
}
