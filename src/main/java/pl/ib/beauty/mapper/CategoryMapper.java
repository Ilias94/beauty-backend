package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dto.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto categoryToCategoryDto(Category category);

    Category categoryDtoToCategory(CategoryDto categoryDto);
}
