package pl.ib.beauty.model.dto;

import pl.ib.beauty.model.dao.Answer;

import java.util.List;

public record QuestionDetailDto(String question, List<Answer> answers, Integer priority) {
}
