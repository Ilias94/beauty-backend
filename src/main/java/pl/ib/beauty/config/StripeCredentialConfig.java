package pl.ib.beauty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
}
