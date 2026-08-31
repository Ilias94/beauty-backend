package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotNull;
import pl.ib.beauty.model.dao.TicketStatus;

public record TicketStatusUpdateDto(@NotNull TicketStatus status) {
}
