package pl.ib.beauty.service


import pl.ib.beauty.model.dao.Category
import pl.ib.beauty.repository.CategoryRepository
import spock.lang.Specification

class CategoryServiceTest extends Specification {
    def categoryRepository = Mock(CategoryRepository)
    def categoryService = new CategoryService(categoryRepository)

    def 'shouldGetAllCategories'() {
        given:
        def hairDresser = new Category("hairDresser")
        def hairStylist = new Category("hairStylist")


        when:
        def result = categoryService.getAllCategories()

        then:
        1 * categoryRepository.findAll() >> [hairDresser, hairStylist]
        0 * _
        result.size() == 2
    }
}
