package pl.ib.beauty.model.dto;

import pl.ib.beauty.model.dao.QuestionDetail;

import java.util.List;

public record QuestionDtoRequest(Long courseId,
                                 List<QuestionDetailDto> questionDetail) {
}
