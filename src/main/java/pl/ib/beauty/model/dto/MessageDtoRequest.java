package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageDtoRequest(
        @NotNull Long recipientId,
        @NotBlank @Size(max = 2000) String content
) {}
