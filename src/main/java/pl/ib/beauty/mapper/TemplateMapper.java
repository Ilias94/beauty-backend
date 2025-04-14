package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Template;
import pl.ib.beauty.model.dto.TemplateDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TemplateMapper {
    TemplateDto templateToDto(Template template);
    List<TemplateDto> templateToDtoList(List<Template> templates);

    Template templateDtoToTemplate(TemplateDto templateDto);
}
