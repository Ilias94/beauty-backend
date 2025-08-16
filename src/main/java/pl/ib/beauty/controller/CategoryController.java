package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.mapper.CategoryMapper;
import pl.ib.beauty.model.dto.CategoryDtoRequest;
import pl.ib.beauty.model.dto.CategoryDtoResponse;
import pl.ib.beauty.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryDtoResponse> getAllCategories() {
        return categoryMapper.categoryToCategoryDto(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public CategoryDtoResponse getCategoryById(@PathVariable Long id) {
        return categoryMapper.categoryToCategoryDto(categoryService.getCategoryById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtoResponse createCategory(@RequestBody @Valid CategoryDtoRequest categoryDtoRequest) {
        return categoryMapper.categoryToCategoryDto(categoryService.createCategory(categoryMapper.categoryDtoToCategory(categoryDtoRequest)));
    }

    @PutMapping("/{id}")
    public CategoryDtoResponse updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryDtoRequest categoryDtoRequest) {

        return categoryMapper.categoryToCategoryDto(categoryService.updateCategory(id, categoryDtoRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
    }
}
