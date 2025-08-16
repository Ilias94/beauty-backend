package pl.ib.beauty.model.dto;

import lombok.Builder;

@Builder
public record AIDtoRequest(String prompt) {
}
