package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveTicketDto(@NotBlank String content) {
}
