package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Gamma;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The PgWeibullDistribution class implements weibull distribution.
 */
public class PgWeibullDistribution implements PgDistribution {
    private final double shape;
    private final double scale;

    /**
     * Constructor.
     */
    public PgWeibullDistribution(double shape, double scale) {
        this.shape = shape;
        this.scale = scale;
        if (shape <= 0) {
            throw new IllegalArgumentException("Shape must be positive");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }
    }

    @Override
    public double pdf(double value) {
        if (value < 0) {
            return 0;
        }
        return (shape / scale) * Math.pow(value / scale, shape - 1) * Math.exp(-Math.pow(value / scale, shape));
    }

    @Override
    public double cdf(double value) {
        if (value < 0) {
            return 0;
        }
        return 1 - Math.exp(-Math.pow(value / scale, shape));
    }

    @Override
    public double mean() {
        return scale * Gamma.gamma(1 + 1 / shape);
    }

    @Override
    public double variance() {
        return scale * scale * (Gamma.gamma(1 + 2 / shape) - Math.pow(Gamma.gamma(1 + 1 / shape), 2));
    }

    @Override
    public double median() {
        return scale * Math.pow(Math.log(2), 1 / shape);
    }

    @Override
    public List<Double> generate(int size, Random random) {
        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double u = random.nextDouble();
            double sample = scale * Math.pow(-Math.log(1 - u), 1 / shape);
            samples.add(sample);
        }
        return samples;
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.WEIBULL;
    }
}
