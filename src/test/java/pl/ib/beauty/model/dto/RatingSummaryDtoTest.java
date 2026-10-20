package pl.ib.beauty.model.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RatingSummaryDtoTest {

    @Test
    void countsRatingsPerStarAndAverages() {
        RatingSummaryDto summary = RatingSummaryDto.of(List.of(5f, 5f, 4f, 2f));

        assertThat(summary.count()).isEqualTo(4);
        assertThat(summary.average()).isEqualTo(4.0);
        assertThat(summary.counts()).containsExactly(0, 1, 0, 1, 2);
    }

    @Test
    void keepsOutOfRangeValuesInTheOuterBuckets() {
        RatingSummaryDto summary = RatingSummaryDto.of(List.of(0f, 4.6f, 7f));

        assertThat(summary.counts()).containsExactly(1, 0, 0, 0, 2);
    }

    @Test
    void isEmptyWithoutRatings() {
        RatingSummaryDto summary = RatingSummaryDto.of(List.of());

        assertThat(summary.count()).isZero();
        assertThat(summary.average()).isZero();
        assertThat(summary.counts()).containsExactly(0, 0, 0, 0, 0);
    }
}
