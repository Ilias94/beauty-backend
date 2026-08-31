package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.ib.beauty.model.dao.TicketType;

public record TicketCreateDto(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull TicketType type) {
}
