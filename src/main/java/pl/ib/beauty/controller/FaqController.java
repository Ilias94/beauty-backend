package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.FaqMapper;
import pl.ib.beauty.model.dto.FaqDtoRequest;
import pl.ib.beauty.model.dto.FaqDtoResponse;
import pl.ib.beauty.service.FaqService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/faq", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;
    private final FaqMapper faqMapper;

    @GetMapping("/search")
    public List<FaqDtoResponse> getByQuestion(@RequestParam String question) {
        return faqMapper.faqsToFaqDtos(faqService.findByQuestion(question));
    }

    @GetMapping
    public List<FaqDtoResponse> getAll() {
        return faqMapper.faqsToFaqDtos(faqService.getAll());
    }

    @GetMapping("/{id}")
    public FaqDtoResponse getById(@PathVariable UUID id) {
        return faqMapper.faqToFaqDto(faqService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaqDtoResponse create(@RequestBody @Valid FaqDtoRequest request) {
        return faqMapper.faqToFaqDto(faqService.create(faqMapper.faqDtoToFaq(request)));
    }

    @PutMapping("/{id}")
    public FaqDtoResponse update(@PathVariable UUID id, @RequestBody @Valid FaqDtoRequest request) {
        return faqMapper.faqToFaqDto(faqService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        faqService.deleteById(id);
    }

    @PostMapping("/reindex")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public void reindexAll() {
        faqService.reindexAll();
    }
}
