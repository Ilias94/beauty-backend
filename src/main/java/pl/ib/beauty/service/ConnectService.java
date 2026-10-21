package pl.ib.beauty.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ConnectStatusDtoResponse;
import pl.ib.beauty.repository.UserRepository;

/** Instructors' Stripe Connect accounts, which course payments are paid out to. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final StripeService stripeService;

    /** Creates the instructor's account on first use and returns a fresh link to Stripe's onboarding. */
    @Transactional
    public String startOnboarding() {
        User instructor = userService.currentLoginUser();
        if (instructor.getStripeAccountId() == null) {
            instructor.setStripeAccountId(stripeService.createConnectedAccount(instructor));
            userRepository.save(instructor);
            log.info("Created Stripe account {} for user {}", instructor.getStripeAccountId(), instructor.getId());
        }
        return stripeService.createOnboardingLink(instructor.getStripeAccountId());
    }

    /**
     * Status of the current instructor's account. Until it is fully enabled it is read from Stripe, so the page
     * an instructor returns to from onboarding is right even before the account.updated webhook arrives.
     */
    @Transactional
    public ConnectStatusDtoResponse getStatus() {
        User instructor = userService.currentLoginUser();
        if (instructor.getStripeAccountId() != null
                && !(instructor.isStripeChargesEnabled() && instructor.isStripePayoutsEnabled())) {
            apply(instructor, stripeService.retrieveAccount(instructor.getStripeAccountId()));
            userRepository.save(instructor);
        }
        return toStatus(instructor);
    }

    public String createDashboardLink() {
        User instructor = userService.currentLoginUser();
        // Stripe only signs in to an Express dashboard once onboarding was submitted.
        if (instructor.getStripeAccountId() == null || !instructor.isStripeDetailsSubmitted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Finish Stripe onboarding first");
        }
        return stripeService.createDashboardLink(instructor.getStripeAccountId());
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        Event event;
        try {
            event = stripeService.constructConnectEvent(payload, signature);
        } catch (SignatureVerificationException e) {
            log.warn("Rejected a Stripe Connect webhook with an invalid signature");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        if ("account.updated".equals(event.getType())) {
            accountUpdated((Account) stripeService.eventObject(event));
        }
    }

    void accountUpdated(Account account) {
        userRepository.findByStripeAccountId(account.getId()).ifPresentOrElse(instructor -> {
            apply(instructor, account);
            userRepository.save(instructor);
            log.info("Stripe account {}: charges {}, payouts {}", account.getId(),
                    instructor.isStripeChargesEnabled(), instructor.isStripePayoutsEnabled());
        }, () -> log.warn("account.updated for unknown Stripe account {}", account.getId()));
    }

    private static void apply(User instructor, Account account) {
        instructor.setStripeDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
        instructor.setStripeChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));
        instructor.setStripePayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
    }

    private static ConnectStatusDtoResponse toStatus(User instructor) {
        return new ConnectStatusDtoResponse(instructor.getStripeAccountId() != null,
                instructor.isStripeDetailsSubmitted(), instructor.isStripeChargesEnabled(),
                instructor.isStripePayoutsEnabled());
    }
}
