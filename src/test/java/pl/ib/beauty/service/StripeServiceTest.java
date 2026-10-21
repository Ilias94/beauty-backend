package pl.ib.beauty.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import pl.ib.beauty.config.StripeCredentialConfig;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripeServiceTest {

    private static final String SECRET = "whsec_test";
    // An API version other than the library's, as `stripe listen` sends with an older account default.
    private static final String PAYLOAD = """
            {"id": "evt_1", "object": "event", "api_version": "2020-08-27", "type": "checkout.session.completed",
             "data": {"object": {"id": "cs_1", "object": "checkout.session",
                                 "client_reference_id": "order-1", "payment_status": "paid"}}}""";

    private final StripeService service = new StripeService(config(SECRET));

    @Test
    void readsAnEventSignedWithTheWebhookSecret() throws Exception {
        Event event = service.constructEvent(PAYLOAD, sign(PAYLOAD, SECRET));

        assertThat(event.getType()).isEqualTo("checkout.session.completed");
        Session session = (Session) service.eventObject(event);
        assertThat(session.getClientReferenceId()).isEqualTo("order-1");
        assertThat(session.getPaymentStatus()).isEqualTo("paid");
    }

    @Test
    void rejectsAnEventSignedWithAnotherSecret() throws Exception {
        String signature = sign(PAYLOAD, "whsec_someone_else");

        assertThatThrownBy(() -> service.constructEvent(PAYLOAD, signature))
                .isInstanceOf(SignatureVerificationException.class);
    }

    @Test
    void rejectsAChangedPayload() throws Exception {
        String signature = sign(PAYLOAD, SECRET);
        String changed = PAYLOAD.replace("order-1", "order-2");

        assertThatThrownBy(() -> service.constructEvent(changed, signature))
                .isInstanceOf(SignatureVerificationException.class);
    }

    @Test
    void rejectsEveryEventWhileNoSecretIsConfigured() throws Exception {
        StripeService unconfigured = new StripeService(config(""));
        String signature = sign(PAYLOAD, SECRET);

        assertThatThrownBy(() -> unconfigured.constructEvent(PAYLOAD, signature))
                .isInstanceOf(SignatureVerificationException.class);
    }

    @Test
    void sendsAmountsInGrosze() {
        assertThat(StripeService.toMinorUnits(new BigDecimal("700.00"))).isEqualTo(70000);
        assertThat(StripeService.toMinorUnits(new BigDecimal("99.99"))).isEqualTo(9999);
        assertThat(StripeService.toMinorUnits(new BigDecimal("700"))).isEqualTo(70000);
    }

    private static String sign(String payload, String secret) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        return "t=" + timestamp + ",v1=" + Webhook.Util.computeHmacSha256(secret, timestamp + "." + payload);
    }

    private static StripeCredentialConfig config(String webhookSecret) {
        StripeCredentialConfig config = new StripeCredentialConfig();
        config.setSecretKey("sk_test_unused");
        config.setWebhookSecret(webhookSecret);
        config.setReturnUrl("http://localhost:4200/payment-status");
        return config;
    }
}
