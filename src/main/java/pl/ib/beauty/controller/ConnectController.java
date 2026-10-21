package pl.ib.beauty.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.dto.ConnectLinkDtoResponse;
import pl.ib.beauty.model.dto.ConnectStatusDtoResponse;
import pl.ib.beauty.service.ConnectService;

/** An instructor's Stripe account for payouts: onboarding, its status and Stripe's dashboard. */
@RestController
@RequestMapping(value = "/api/v1/connect", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ConnectController {
    private static final String TEACHER = "hasAuthority('SCOPE_TEACHER')";

    private final ConnectService connectService;

    @PostMapping("/onboarding")
    @PreAuthorize(TEACHER)
    @Operation(operationId = "startStripeOnboarding")
    public ConnectLinkDtoResponse startOnboarding() {
        return new ConnectLinkDtoResponse(connectService.startOnboarding());
    }

    @GetMapping("/status")
    @PreAuthorize(TEACHER)
    @Operation(operationId = "getStripeConnectStatus")
    public ConnectStatusDtoResponse getStatus() {
        return connectService.getStatus();
    }

    @PostMapping("/dashboard-link")
    @PreAuthorize(TEACHER)
    @Operation(operationId = "createStripeDashboardLink")
    public ConnectLinkDtoResponse createDashboardLink() {
        return new ConnectLinkDtoResponse(connectService.createDashboardLink());
    }

    // Called by Stripe, not the frontend: the raw body is needed to check the signature.
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(hidden = true)
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        connectService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
