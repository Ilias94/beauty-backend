package pl.ib.beauty.model.dto;

import pl.ib.beauty.model.dao.QuestionDetail;

public record QuestionDtoResponse(Long id,
                                  Integer priority,
                                  QuestionDetail questionDetail) {
}
