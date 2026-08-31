package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseMessageDtoRequest(
        @NotBlank @Size(max = 2000) String content
) {}
