package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageDtoRequest(
        @NotNull Long recipientId,
        @NotBlank @Size(max = 2000) String content,
        // Optional: the course the message is about (e.g. a question sent from its page).
        Long courseId
) {}
