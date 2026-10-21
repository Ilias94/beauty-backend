package pl.ib.beauty.model.dto;

import pl.ib.beauty.model.Status;

import java.util.UUID;

public record PaymentStatusDtoResponse(UUID orderId, Status status, Long courseId, String courseTitle) {
}
