package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import java.util.Locale;
import java.util.Objects;


/**
 * Represents bin in density histogram with lower bound, upper bound and height.
 * Each bin covers specific range of values with corresponding height.
 * Instances of this class are immutable.
 */

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
        if (upper - lower <= 0) {
            throw new IllegalArgumentException("Upper must be greater than lower");
        }

        this.lower = lower;
        this.upper = upper;
        this.height = height;
    }

    /**
     * Returns middle value of bin, calculated as average of lower and upper bounds.
     *
     * @return middle value of bin.
     */
    public double getMiddle() {
        return (lower + upper) / 2;
    }

    /**
     * Returns lower bound of bin.
     *
     * @return lower bound of bin.
     */
    public double lower() {
        return lower;
    }

    /**
     * Returns upper bound of bin.
     *
     * @return upper bound of bin.
     */
    public double upper() {
        return upper;
    }

    /**
     * Returns height of bin.
     *
     * @return height of bin.
     */
    public double height() {
        return height;
    }

    /**
     * Returns string representation of bin in format {@code [lower; upper] H = height}.
     *
     * @return string representation of bin.
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "[%s; %s] H = %s",
                String.valueOf(lower),
                String.valueOf(upper),
                String.valueOf(height));
    }

    /**
     * Returns hash code for this bin based on its lower bound, upper bound and height.
     *
     * @return hash code of this bin.
     */
    @Override
    public int hashCode() {
        return Objects.hash(lower, upper, height);
    }
}
