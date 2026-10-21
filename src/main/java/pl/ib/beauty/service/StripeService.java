package pl.ib.beauty.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.ib.beauty.config.StripeCredentialConfig;
import pl.ib.beauty.model.dao.Course;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class StripeService {
    public static final String CURRENCY = "PLN";
    // Stripe's minimum; an abandoned checkout expires and its payment is marked FAILED.
    private static final Duration CHECKOUT_LIFETIME = Duration.ofMinutes(30);

    private final StripeCredentialConfig stripeCredentialConfig;

    public StripeService(StripeCredentialConfig stripeCredentialConfig) {
        this.stripeCredentialConfig = stripeCredentialConfig;
        Stripe.apiKey = stripeCredentialConfig.getSecretKey();
    }

    @SneakyThrows
    public Session createSession(Course course, UUID orderId, String customerEmail) {
        String returnUrl = stripeCredentialConfig.getReturnUrl() + "?orderId=" + orderId;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(orderId.toString())
                .putMetadata("orderId", orderId.toString())
                .setCustomerEmail(customerEmail)
                .setExpiresAt(Instant.now().plus(CHECKOUT_LIFETIME).getEpochSecond())
                .setSuccessUrl(returnUrl)
                .setCancelUrl(returnUrl + "&cancelled=true")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY)
                                .setUnitAmount(toMinorUnits(course.getPrice()))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(course.getTitle())
                                        .build())
                                .build())
                        .build())
                .build();
        return Session.create(params);
    }

    /** Verifies the Stripe-Signature header, so only Stripe can report a payment as paid. */
    public Event constructEvent(String payload, String signature) throws SignatureVerificationException {
        String secret = stripeCredentialConfig.getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            throw new SignatureVerificationException("STRIPE_WEBHOOK_SECRET is not set", signature);
        }
        return Webhook.constructEvent(payload, signature, secret);
    }

    /** The event's object, also when the event was sent with an API version other than the library's. */
    @SneakyThrows
    public StripeObject eventObject(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        return deserializer.getObject().isPresent() ? deserializer.getObject().get() : deserializer.deserializeUnsafe();
    }

    /** Stripe takes amounts in the smallest currency unit: 700.00 zł is 70000 grosze. */
    static long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).longValueExact();
    }
}
