package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.mapper.CategoryMapper;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dto.CategoryDto;
import pl.ib.beauty.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private static final String ENTITY_NOT_FOUND_MESSAGE = "Category not found with id: ";
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::categoryToCategoryDto)
                .toList();
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE + id));
        return categoryMapper.categoryToCategoryDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = categoryMapper.categoryDtoToCategory(categoryDto);
        category = categoryRepository.save(category);
        return categoryMapper.categoryToCategoryDto(category);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE + id));

        existingCategory.setLabel(categoryDto.getLabel());
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.categoryToCategoryDto(updatedCategory);
    }

    @Transactional
    public void deleteCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE + id);
        }
        categoryRepository.deleteById(id);
    }
}
