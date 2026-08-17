package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;

public record TemplateDto(
        @NotBlank(message = "Name cannot be blank") String name,
        @NotBlank(message = "Body cannot be blank") String body,
        @NotBlank(message = "Subject cannot be blank") String subject) {
}
