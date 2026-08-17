package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import pl.ib.beauty.model.dao.Question;
import pl.ib.beauty.model.dto.QuestionDetailDto;
import pl.ib.beauty.model.dto.QuestionDtoResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuestionMapper {
    List<Question> toQuestion(List<QuestionDetailDto> questionDtoRequest);

    QuestionDtoResponse toResponse(Question question);
    List<QuestionDtoResponse> toResponses(List<Question> question);
}
