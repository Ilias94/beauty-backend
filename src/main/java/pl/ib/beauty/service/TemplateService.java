package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Template;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.TemplateRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final TemplateRepository templateRepository;

    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    public Template findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template with id " + id + " not found"));
    }

    public Template findByName(String templateName) {
        return templateRepository.findByName(templateName)
                .orElseThrow(() -> new EntityNotFoundException("Template with name " + templateName + " not found"));
    }

    public Template create(Template template) {
        return templateRepository.save(template);
    }

    public Template update(Long id, Template templateDetails) {
        Template existingTemplate = findById(id);
        existingTemplate.setName(templateDetails.getName());
        existingTemplate.setBody(templateDetails.getBody());
        existingTemplate.setSubject(templateDetails.getSubject());
        return templateRepository.save(existingTemplate);
    }

    public void delete(Long id) {
        Template existingTemplate = findById(id);
        templateRepository.delete(existingTemplate);
    }

    public Map<String, Object> prepareTemplateVariables(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("lastName", user.getLastName());
        variables.put("roleName", user.getRoleList().stream().findFirst().orElseThrow().getName()); // Assuming the user always has one role
        return variables;
    }


}
