package pl.ib.beauty.model.dto;

import java.time.LocalDateTime;

public record RepublishCourseRequest(LocalDateTime newStartDate, LocalDateTime newEndDate) {
}
