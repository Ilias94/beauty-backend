package pl.ib.beauty.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.ConnectStatusDtoResponse;
import pl.ib.beauty.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectServiceTest {

    private final UserService userService = mock(UserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StripeService stripeService = mock(StripeService.class);

    private final ConnectService service = new ConnectService(userService, userRepository, stripeService);

    private final User instructor = User.builder().id(7L).email("marta@test.pl").build();

    @BeforeEach
    void setUp() {
        when(userService.currentLoginUser()).thenReturn(instructor);
        when(stripeService.createOnboardingLink(anyString())).thenReturn("https://connect.stripe.com/setup/e/acct_1");
    }

    @Test
    void firstOnboardingCreatesTheAccount() {
        when(stripeService.createConnectedAccount(instructor)).thenReturn("acct_1");

        assertThat(service.startOnboarding()).isEqualTo("https://connect.stripe.com/setup/e/acct_1");
        assertThat(instructor.getStripeAccountId()).isEqualTo("acct_1");
        verify(userRepository).save(instructor);
    }

    @Test
    void resumedOnboardingReusesTheAccount() {
        instructor.setStripeAccountId("acct_1");

        service.startOnboarding();

        verify(stripeService, never()).createConnectedAccount(any());
        verify(stripeService).createOnboardingLink("acct_1");
    }

    @Test
    void statusWithoutAnAccountDoesNotCallStripe() {
        assertThat(service.getStatus()).isEqualTo(new ConnectStatusDtoResponse(false, false, false, false));
        verify(stripeService, never()).retrieveAccount(anyString());
    }

    @Test
    void statusOfAnUnfinishedAccountIsReadFromStripe() {
        instructor.setStripeAccountId("acct_1");
        when(stripeService.retrieveAccount("acct_1")).thenReturn(account("acct_1", true, true, false));

        assertThat(service.getStatus()).isEqualTo(new ConnectStatusDtoResponse(true, true, true, false));
        assertThat(instructor.isStripeChargesEnabled()).isTrue();
    }

    @Test
    void statusOfAnEnabledAccountComesFromTheDatabase() {
        instructor.setStripeAccountId("acct_1");
        instructor.setStripeDetailsSubmitted(true);
        instructor.setStripeChargesEnabled(true);
        instructor.setStripePayoutsEnabled(true);

        assertThat(service.getStatus()).isEqualTo(new ConnectStatusDtoResponse(true, true, true, true));
        verify(stripeService, never()).retrieveAccount(anyString());
    }

    @Test
    void dashboardNeedsFinishedOnboarding() {
        instructor.setStripeAccountId("acct_1");

        assertThatThrownBy(service::createDashboardLink)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void accountUpdatedRecordsWhatStripeAllows() {
        instructor.setStripeAccountId("acct_1");
        instructor.setStripeChargesEnabled(true);
        when(userRepository.findByStripeAccountId("acct_1")).thenReturn(Optional.of(instructor));

        // Stripe can also take permissions away, e.g. when it needs more documents.
        service.accountUpdated(account("acct_1", true, false, true));

        assertThat(instructor.isStripeChargesEnabled()).isFalse();
        assertThat(instructor.isStripePayoutsEnabled()).isTrue();
        verify(userRepository).save(instructor);
    }

    @Test
    void webhookWithABadSignatureIsRejected() throws Exception {
        when(stripeService.constructConnectEvent("{}", "t=1,v1=forged"))
                .thenThrow(new SignatureVerificationException("bad", "t=1,v1=forged"));

        assertThatThrownBy(() -> service.handleWebhook("{}", "t=1,v1=forged"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private static Account account(String id, boolean detailsSubmitted, boolean chargesEnabled, boolean payoutsEnabled) {
        Account account = new Account();
        account.setId(id);
        account.setDetailsSubmitted(detailsSubmitted);
        account.setChargesEnabled(chargesEnabled);
        account.setPayoutsEnabled(payoutsEnabled);
        return account;
    }
}
