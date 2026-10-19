package pl.ib.beauty.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentMap;

/**
 * Caps how many assistant questions one user can ask per day, so a single account cannot
 * run up the Anthropic bill. Counters live in a map shared by all replicas (a Hazelcast
 * IMap in production, see AssistantConfig) and are keyed by user and date.
 */
public class AssistantUsageLimiter {

    private final ConcurrentMap<String, Integer> usage;
    private final int dailyLimit;
    private final Clock clock;

    public AssistantUsageLimiter(ConcurrentMap<String, Integer> usage, int dailyLimit, Clock clock) {
        this.usage = usage;
        this.dailyLimit = dailyLimit;
        this.clock = clock;
    }

    /**
     * Counts one question for the user.
     *
     * @return how many questions the user has left today after this one, or -1 when the limit
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
