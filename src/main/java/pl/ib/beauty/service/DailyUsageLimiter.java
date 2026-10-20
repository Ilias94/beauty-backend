package pl.ib.beauty.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentMap;

/**
 * Caps how many times one user can do something per day: assistant questions (so a single
 * account cannot run up the Anthropic bill) and new private conversations (against spam).
 * Counters live in a map shared by all replicas (a Hazelcast IMap in production, see
 * AssistantConfig and MessagingConfig) and are keyed by user and date.
 */
public class DailyUsageLimiter {

    private final ConcurrentMap<String, Integer> usage;
    private final int dailyLimit;
    private final Clock clock;

    public DailyUsageLimiter(ConcurrentMap<String, Integer> usage, int dailyLimit, Clock clock) {
        this.usage = usage;
        this.dailyLimit = dailyLimit;
        this.clock = clock;
    }

    /**
     * Counts one use for the user.
     *
     * @return how many uses the user has left today after this one, or -1 when the limit
     * was already reached (nothing is counted then)
     */
    public int tryAcquire(String userEmail) {
        String key = key(userEmail);
        while (true) {
            Integer used = usage.putIfAbsent(key, 1);
            if (used == null) {
                return dailyLimit - 1;
            }
            if (used >= dailyLimit) {
                return -1;
            }
            if (usage.replace(key, used, used + 1)) {
                return dailyLimit - used - 1;
            }
        }
    }

    public int remaining(String userEmail) {
        return Math.max(0, dailyLimit - usage.getOrDefault(key(userEmail), 0));
    }

    private String key(String userEmail) {
        return userEmail + "|" + LocalDate.now(clock);
    }
}
