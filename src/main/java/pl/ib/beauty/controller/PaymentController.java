package pl.ib.beauty.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.dto.CreatePaymentDtoRequest;
import pl.ib.beauty.model.dto.CreatePaymentDtoResponse;
import pl.ib.beauty.model.dto.PaymentStatusDtoResponse;
import pl.ib.beauty.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/payment", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CreatePaymentDtoResponse createPayment(@RequestBody CreatePaymentDtoRequest createPaymentDto) {
        String payment = paymentService.createPayment(createPaymentDto.courseId());
        return new CreatePaymentDtoResponse(payment);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getPaymentStatus")
    public PaymentStatusDtoResponse getPaymentStatus(@PathVariable UUID orderId) {
        return paymentService.getPaymentStatus(orderId);
    }

    // Called by Stripe, not the frontend: the raw body is needed to check the signature.
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(hidden = true)
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
