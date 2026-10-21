package pl.ib.beauty.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Event;
import com.stripe.model.LoginLink;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.ib.beauty.config.StripeCredentialConfig;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class StripeService {
    public static final String CURRENCY = "PLN";
    // Stripe's minimum; an abandoned checkout expires and its payment is marked FAILED.
    private static final Duration CHECKOUT_LIFETIME = Duration.ofMinutes(30);
    // Schools and Educational Services, Not Elsewhere Classified.
    private static final String EDUCATIONAL_SERVICES_MCC = "8299";
    private static final String INSTRUCTOR_PRODUCT_DESCRIPTION =
            "Szkolenia i kursy z branży beauty (online i stacjonarne), sprzedawane przez platformę Bellademy (bellademy.pl).";

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

    /**
     * An Express account: Stripe onboards and verifies the instructor and gives them its Express dashboard,
     * the platform pays Stripe's fees and covers refunds and disputes the instructor's balance can't.
     */
    @SneakyThrows
    public String createConnectedAccount(User instructor) {
        AccountCreateParams params = AccountCreateParams.builder()
                .setCountry("PL")
                .setEmail(instructor.getEmail())
                .setController(AccountCreateParams.Controller.builder()
                        .setFees(AccountCreateParams.Controller.Fees.builder()
                                .setPayer(AccountCreateParams.Controller.Fees.Payer.APPLICATION).build())
                        .setLosses(AccountCreateParams.Controller.Losses.builder()
                                .setPayments(AccountCreateParams.Controller.Losses.Payments.APPLICATION).build())
                        .setStripeDashboard(AccountCreateParams.Controller.StripeDashboard.builder()
                                .setType(AccountCreateParams.Controller.StripeDashboard.Type.EXPRESS).build())
                        .build())
                .setCapabilities(AccountCreateParams.Capabilities.builder()
                        .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
                        .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                        .build())
                // Answers onboarding's industry and website questions up front: instructors often have no
                // website of their own, and Stripe takes a product description in its place.
                .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                        .setMcc(EDUCATIONAL_SERVICES_MCC)
                        .setProductDescription(INSTRUCTOR_PRODUCT_DESCRIPTION)
                        .build())
                .putMetadata("userId", String.valueOf(instructor.getId()))
                .build();
        return Account.create(params).getId();
    }

    /** A one-time link to Stripe's hosted onboarding; it expires within minutes, so it is made on every click. */
    @SneakyThrows
    public String createOnboardingLink(String accountId) {
        String payoutsUrl = stripeCredentialConfig.getPayoutsUrl();
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .setRefreshUrl(payoutsUrl + "?onboarding=expired")
                .setReturnUrl(payoutsUrl + "?onboarding=returned")
                .build();
        return AccountLink.create(params).getUrl();
    }

    /** A one-time sign-in to the instructor's Express dashboard (payouts, balance, bank account). */
    @SneakyThrows
    public String createDashboardLink(String accountId) {
        return LoginLink.createOnAccount(accountId).getUrl();
    }

    @SneakyThrows
    public Account retrieveAccount(String accountId) {
        return Account.retrieve(accountId);
    }

    /** Verifies the Stripe-Signature header, so only Stripe can report a payment as paid. */
    public Event constructEvent(String payload, String signature) throws SignatureVerificationException {
        return verify(payload, signature, stripeCredentialConfig.getWebhookSecret(), "STRIPE_WEBHOOK_SECRET");
    }

    /** Same check for the Connect endpoint, which Stripe signs with its own secret. */
    public Event constructConnectEvent(String payload, String signature) throws SignatureVerificationException {
        return verify(payload, signature, stripeCredentialConfig.getConnectWebhookSecret(), "STRIPE_CONNECT_WEBHOOK_SECRET");
    }

    private static Event verify(String payload, String signature, String secret, String variable)
            throws SignatureVerificationException {
        if (secret == null || secret.isBlank()) {
            throw new SignatureVerificationException(variable + " is not set", signature);
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
