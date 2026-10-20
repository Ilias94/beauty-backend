package pl.ib.beauty.model.dto;

import java.util.Arrays;
import java.util.List;

/**
 * A course's ratings at a glance. {@code counts} has five entries: index 0 holds the number
 * of one-star ratings, index 4 the number of five-star ones.
 */
public record RatingSummaryDto(double average, int count, List<Integer> counts) {

    public static RatingSummaryDto of(List<Float> values) {
        int[] counts = new int[5];
        double sum = 0;
        for (float value : values) {
            counts[Math.clamp(Math.round(value), 1, 5) - 1]++;
            sum += value;
        }
        double average = values.isEmpty() ? 0 : sum / values.size();
        return new RatingSummaryDto(average, values.size(), Arrays.stream(counts).boxed().toList());
    }
}
