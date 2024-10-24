package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;


/**
 * Represents bin in density histogram with lower bound, upper bound and height.
 * Each bin covers specific range of values with corresponding height.
 * Instances of this class are immutable.
 */

@Getter
@EqualsAndHashCode
public final class DensityHistogramBin {
    private final double lower;
    private final double upper;
    private final double height;

    /**
     * Constructs {@code DensityHistogramBin} with specified lower bound, upper bound and height.
     *
     * @param lower  lower bound of bin.
     * @param upper  upper bound of bin. Must be greater than {@code lower}.
     * @param height height of bin. Must be non-negative.
     * @throws IllegalArgumentException if {@code height} is negative or {@code upper} is not greater than {@code lower}.
     */
    public DensityHistogramBin(double lower, double upper, double height) {
        if (height < 0) {
            throw new IllegalArgumentException("Height cannot be negative");
        }
        if (upper <= lower) {
            throw new IllegalArgumentException("Upper must be greater than lower");
        }

        this.lower = lower;
        this.upper = upper;
        this.height = height;
    }

    /**
     * Returns middle value of bin, calculated as the average of lower and upper bounds.
     *
     * @return middle value of bin.
     */
    public double getMiddle() {
        return (lower + upper) / 2;
    }

    /**
     * Returns string representation of bin in format {@code [lower; upper] H = height}.
     *
     * @return string representation of bin.
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "[%s; %s] H = %s", lower, upper, height);
    }
}
