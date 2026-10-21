package pl.ib.beauty.model.dto;

/**
 * Where an instructor is with Stripe: no account yet, onboarding unfinished, under review,
 * or able to sell (chargesEnabled) and be paid out (payoutsEnabled).
 */
public record ConnectStatusDtoResponse(boolean accountCreated, boolean detailsSubmitted,
                                       boolean chargesEnabled, boolean payoutsEnabled) {
}
