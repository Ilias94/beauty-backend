package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.Status;
import pl.ib.beauty.model.dto.CreatePaymentDtoRequest;
import pl.ib.beauty.model.dto.CreatePaymentDtoResponse;
import pl.ib.beauty.service.PaymentService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/payment", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public CreatePaymentDtoResponse createPayment(@RequestBody CreatePaymentDtoRequest createPaymentDto) {
        String payment = paymentService.createPayment(createPaymentDto.getCourseId());
        return new CreatePaymentDtoResponse(payment);
    }

    @GetMapping("/notify")
    public ResponseEntity<Object> notify(@RequestParam Status status, @RequestParam UUID corelationKey) {
        paymentService.updateStatus(corelationKey, status);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:4200/payment-status?status=" + status))
                .build();
    }
}
