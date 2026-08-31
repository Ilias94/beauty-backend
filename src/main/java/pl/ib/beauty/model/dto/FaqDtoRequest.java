package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FaqDtoRequest(
        @NotBlank String question,
        @NotBlank String answer
) {
}
