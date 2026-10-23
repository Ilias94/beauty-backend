package pl.ib.beauty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "stripe")
@Configuration
@Getter
@Setter
public class StripeCredentialConfig {
    private String secretKey;
    // Signs the events Stripe posts to /api/v1/payment/webhook (whsec_...).
    private String webhookSecret;
    // Frontend page Stripe Checkout sends the buyer back to, with ?orderId=... appended.
    private String returnUrl;
    // Signs Connect events (account.updated) posted to /api/v1/connect/webhook; a separate endpoint in Stripe.
    private String connectWebhookSecret;
    // Frontend page an instructor returns to from Stripe's onboarding.
    private String payoutsUrl;
    // The platform's cut of a course sale in percent, unless the instructor has their own (users.platform_fee_percent).
    private BigDecimal platformFeePercent = BigDecimal.TEN;
}
