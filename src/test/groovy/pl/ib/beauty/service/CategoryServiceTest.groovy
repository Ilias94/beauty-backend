package pl.ib.beauty.service

import pl.ib.beauty.mapper.CategoryMapper
import pl.ib.beauty.model.dao.Category
import pl.ib.beauty.model.dto.CategoryDto
import pl.ib.beauty.repository.CategoryRepository
import spock.lang.Specification

class CategoryServiceTest extends Specification {
    def categoryMapper = Mock(CategoryMapper)
    def categoryRepository = Mock(CategoryRepository)
    def categoryService = new CategoryService(categoryMapper, categoryRepository)

    def 'shouldGetAllCategories'() {
        given:
        def hairDresser = new Category("hairDresser")
        def hairStylist = new Category("hairStylist")
        def hairDresserDto = new CategoryDto(1, "hairDresser")
        def hairStylistDto = new CategoryDto(2, "hairStylist")


        when:
        def result = categoryService.getAllCategories()

        then:
        1 * categoryRepository.findAll() >> [hairDresser, hairStylist]
        1 * categoryMapper.categoryToCategoryDto(hairDresser) >> hairDresserDto
        1 * categoryMapper.categoryToCategoryDto(hairStylist) >> hairStylistDto
        0 * _
        result.size() == 2
    }
}
