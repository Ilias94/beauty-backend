package pl.ib.beauty.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.TemplateMapper;
import pl.ib.beauty.model.dto.TemplateDto;
import pl.ib.beauty.service.TemplateService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/templates")
// E-mail templates are rendered by Thymeleaf and sent in the platform's name: admin only.
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateMapper templateMapper;


    @GetMapping
    public List<TemplateDto> getAllTemplates() {
        return templateMapper.templateToDtoList(templateService.findAll());
    }

    @GetMapping("/{id}")
    public TemplateDto getTemplateById(@PathVariable Long id) {
        return templateMapper.templateToDto(templateService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateDto createTemplate(@Valid @RequestBody TemplateDto template) {
        return templateMapper.templateToDto(templateService.create(templateMapper.templateDtoToTemplate(template)));
    }

    @PutMapping("/{id}")
    public TemplateDto updateTemplate(@PathVariable Long id, @Valid @RequestBody TemplateDto templateDetails) {
        return templateMapper.templateToDto(templateService.update(id, templateMapper.templateDtoToTemplate(templateDetails)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable Long id) {
        templateService.delete(id);
    }
}
