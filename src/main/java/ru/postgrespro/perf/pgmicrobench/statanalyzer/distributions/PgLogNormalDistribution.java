package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The PgLogNormalDistribution class implements log-normal distribution.
 */
public class PgLogNormalDistribution implements PgDistribution {
    private final double mean;
    private final double standardDeviation;

    /**
     * Constructor.
     */
    public PgLogNormalDistribution(double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation must be positive");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    @Override
    public double pdf(double value) {
        if (value <= 0) {
            return 0;
        }
        double logValue = Math.log(value);
        return (1 / (value * standardDeviation * Math.sqrt(2 * Math.PI)))
                * Math.exp(-Math.pow(logValue - mean, 2) / (2 * standardDeviation * standardDeviation));
    }

    @Override
    public double cdf(double value) {
        if (value <= 0) {
            return 0;
        }
        double logValue = Math.log(value);
        return 0.5 * (1 + Erf.erf((logValue - mean) / (standardDeviation * Math.sqrt(2))));
    }

    @Override
    public double mean() {
        return Math.exp(mean + standardDeviation * standardDeviation / 2);
    }

    @Override
    public double variance() {
        return (Math.exp(standardDeviation * standardDeviation) - 1) * Math.exp(2 * mean + standardDeviation * standardDeviation);
    }

    @Override
    public double median() {
        return Math.exp(mean);
    }

    @Override
    public List<Double> generate(int size, Random random) {
        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double sample = Math.exp(mean + standardDeviation * random.nextGaussian());
            samples.add(sample);
        }
        return samples;
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.LOGNORMAL;
    }
}
