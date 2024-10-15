package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.util.Locale;
import java.util.Objects;

public final class DensityHistogramBin {
    private final double lower;
    private final double upper;
    private final double height;

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

    public double getMiddle() {
        return (lower + upper) / 2;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "[%s; %s] H = %s",
                String.valueOf(lower),
                String.valueOf(upper),
                String.valueOf(height));
    }

    public double lower() {
        return lower;
    }

    public double upper() {
        return upper;
    }

    public double height() {
        return height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower, upper, height);
    }
}
