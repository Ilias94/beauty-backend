package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDtoRequest(
        Long userId,
        @NotBlank String oldPassword,
        @NotBlank String newPassword,
        @NotBlank String confirmNewPassword) {
}
