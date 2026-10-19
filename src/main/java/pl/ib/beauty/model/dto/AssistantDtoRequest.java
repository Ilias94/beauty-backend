package pl.ib.beauty.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * One question to the assistant. The client keeps the conversation and sends the recent
 * turns back as history; the server stays stateless.
 */
public record AssistantDtoRequest(
        @NotBlank @Size(max = 1000) String message,
        @Size(max = 10) List<@Valid Turn> history) {

    public record Turn(
            @Pattern(regexp = "user|assistant") String role,
            @NotBlank @Size(max = 4000) String content) {
    }
}
