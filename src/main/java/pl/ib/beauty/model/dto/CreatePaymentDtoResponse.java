package pl.ib.beauty.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaymentDtoResponse {
    private String paymentUrl;
}
