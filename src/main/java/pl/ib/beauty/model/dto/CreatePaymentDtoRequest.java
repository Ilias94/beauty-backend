package pl.ib.beauty.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class CreatePaymentDtoRequest {
    private Long courseId;
}
