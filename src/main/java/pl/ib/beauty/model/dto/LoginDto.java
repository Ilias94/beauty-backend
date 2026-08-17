package pl.ib.beauty.model.dto;

import jakarta.validation.constraints.NotNull;

public record LoginDto(@NotNull String email, @NotNull String password) {
}
