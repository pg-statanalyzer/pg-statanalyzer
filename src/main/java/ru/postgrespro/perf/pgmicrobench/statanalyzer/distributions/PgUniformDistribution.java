package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Represents uniform distribution, which generates random numbers uniformly
 * distributed between specified minimum and maximum value.
 */

public class PgUniformDistribution implements PgDistribution {
    private final double min;
    private final double max;

    public PgUniformDistribution(double min, double max) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must be greater than Min.");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Generates list of random numbers uniformly distributed between specified minimum and maximum values.
     *
     * @param random random number generator to use.
     * @param min    minimum value (inclusive) of range.
     * @param max    maximum value (exclusive) of range.
     * @param count  number of random values to generate.
     * @return list containing generated random values.
     * @throws IllegalArgumentException if {@code max} is less than or equal to {@code min} or {@code count} is negative.
     */
    public static List<Double> generate(Random random, double min, double max, int count) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must be greater than Min.");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative.");
        }

        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double value = min + (max - min) * random.nextDouble();
            values.add(value);
        }
        return values;
    }

    @Override
    public double pdf(double value) {
        if (value < min || value >= max) {
            return 0;
        }
        return 1 / (max - min);
    }

    @Override
    public double cdf(double value) {
        if (value < min) {
            return 0;
        }
        if (value >= max) {
            return 1;
        }
        return (value - min) / (max - min);
    }

    @Override
    public List<Double> generate(int size, Random random) {
        return generate(random, min, max, size);
    }
}
