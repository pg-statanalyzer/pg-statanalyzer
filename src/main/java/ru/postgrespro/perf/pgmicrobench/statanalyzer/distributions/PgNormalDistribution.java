package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The PgNormalDistribution class implements normal distribution.
 */
public class PgNormalDistribution implements PgDistribution {
    private final double mean;
    private final double standardDeviation;

    /**
     * Constructor.
     */
    public PgNormalDistribution(double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation must be positive");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Generates list of random numbers following normal (Gaussian) distribution.
     *
     * @param size   number of values to generate.
     * @param mean   mean of distribution.
     * @param stdDev standard deviation of distribution.
     * @return list of randomly generated numbers.
     */
    public static List<Double> generate(int size, double mean, double stdDev) {
        Random random = new Random();
        List<Double> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.add(mean + stdDev * random.nextGaussian());
        }
        return data;
    }

    @Override
    public double pdf(double value) {
        double exponent = -((value - mean) * (value - mean)) / (2 * standardDeviation * standardDeviation);
        return (1 / (standardDeviation * Math.sqrt(2 * Math.PI))) * Math.exp(exponent);
    }

    @Override
    public double cdf(double value) {
        return 0.5 * (1 + Erf.erf((value - mean) / (standardDeviation * Math.sqrt(2))));
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double variance() {
        return standardDeviation * standardDeviation;
    }

    @Override
    public double median() {
        return mean;
    }

    @Override
    public Sample generate(int size, Random random) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double sample = mean + standardDeviation * random.nextGaussian();
            values.add(sample);
        }
        return new Sample(values);
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.NORMAL;
    }

}
