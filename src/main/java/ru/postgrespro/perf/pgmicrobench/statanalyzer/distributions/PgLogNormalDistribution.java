package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

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
        return (1 / (value * standardDeviation * sqrt(2 * Math.PI)))
                * exp(-Math.pow(logValue - mean, 2) / (2 * standardDeviation * standardDeviation));
    }

    @Override
    public double cdf(double value) {
        if (value <= 0) {
            return 0;
        }
        double logValue = Math.log(value);
        return 0.5 * (1 + Erf.erf((logValue - mean) / (standardDeviation * sqrt(2))));
    }

    @Override
    public double mean() {
        return exp(mean + standardDeviation * standardDeviation / 2);
    }

    @Override
    public double variance() {
        return (exp(standardDeviation * standardDeviation) - 1) * exp(2 * mean + standardDeviation * standardDeviation);
    }

    @Override
    public double median() {
        return exp(mean);
    }


    @Override
    public double skewness() {
        return (exp(standardDeviation * standardDeviation) + 2) * sqrt(expm1(standardDeviation * standardDeviation));
    }


    @Override
    public double kurtosis() {
        return 3 * exp(2 * standardDeviation * standardDeviation)
                + 2 * exp(3 * standardDeviation * standardDeviation)
                + exp(4 * standardDeviation * standardDeviation) - 3;
    }


    @Override
    public Sample generate(int size, Random random) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double sample = exp(mean + standardDeviation * random.nextGaussian());
            values.add(sample);
        }
        return  new Sample(values);
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.LOGNORMAL;
    }

    @Override
    public String toString() {
        return "LogNormal(mean=" + String.format("%.2f", mean) + ", standardDeviation=" + String.format("%.2f", standardDeviation) + ")";
    }
}
