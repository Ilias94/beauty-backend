package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Template;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByName(String templateName);
}
