package pl.ib.beauty.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class DailyUsageLimiterTest {

    private static final Instant DAY_1 = Instant.parse("2026-10-19T10:00:00Z");

    private final ConcurrentHashMap<String, Integer> usage = new ConcurrentHashMap<>();

    @Test
    void countsDownAndBlocksAtTheLimit() {
        DailyUsageLimiter limiter = limiterAt(DAY_1, 3);

        assertThat(limiter.tryAcquire("anna@test.pl")).isEqualTo(2);
        assertThat(limiter.tryAcquire("anna@test.pl")).isEqualTo(1);
        assertThat(limiter.tryAcquire("anna@test.pl")).isZero();
        assertThat(limiter.tryAcquire("anna@test.pl")).isEqualTo(-1);
        assertThat(limiter.remaining("anna@test.pl")).isZero();
    }

    @Test
    void blockedAttemptsAreNotCounted() {
        DailyUsageLimiter limiter = limiterAt(DAY_1, 1);
        limiter.tryAcquire("anna@test.pl");

        limiter.tryAcquire("anna@test.pl");
        limiter.tryAcquire("anna@test.pl");

        assertThat(usage).containsValue(1);
    }

    @Test
    void usersHaveSeparateLimits() {
        DailyUsageLimiter limiter = limiterAt(DAY_1, 1);

        assertThat(limiter.tryAcquire("anna@test.pl")).isZero();
        assertThat(limiter.tryAcquire("ola@test.pl")).isZero();
    }

    @Test
    void limitResetsTheNextDay() {
        limiterAt(DAY_1, 1).tryAcquire("anna@test.pl");

        DailyUsageLimiter nextDay = limiterAt(DAY_1.plusSeconds(24 * 3600), 1);

        assertThat(nextDay.tryAcquire("anna@test.pl")).isZero();
    }

    private DailyUsageLimiter limiterAt(Instant now, int dailyLimit) {
        return new DailyUsageLimiter(usage, dailyLimit, Clock.fixed(now, ZoneOffset.UTC));
    }
}
