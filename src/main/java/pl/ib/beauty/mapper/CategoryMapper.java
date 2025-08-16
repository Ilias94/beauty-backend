package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dto.CategoryDtoRequest;
import pl.ib.beauty.model.dto.CategoryDtoResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDtoResponse categoryToCategoryDto(Category category);
    List<CategoryDtoResponse> categoryToCategoryDto(List<Category> category);

    Category categoryDtoToCategory(CategoryDtoRequest categoryDtoRequest);
}
