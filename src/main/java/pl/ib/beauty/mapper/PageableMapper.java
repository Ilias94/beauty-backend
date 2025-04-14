package pl.ib.beauty.mapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageableMapper {
    public Pageable createPageable(int page, int size, Sort.Direction sortDirection, String sortBy) {
        Sort sort = Sort.by(sortBy);
        if (sortDirection != null) {
            sort = Sort.by(sortDirection, sortBy);
        }
        return PageRequest.of(page, size, sort);
    }
}
