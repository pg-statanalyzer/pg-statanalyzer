package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The PgNormalDistribution class implements normal distribution.
 */
public class PgNormalDistribution implements PgSimpleDistribution {
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
    public double skewness() {
        return 0;
    }


    @Override
    public double kurtosis() {
        return 3;
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

    @Override
    public int getParamNumber() {
        return 2;
    }

    @Override
    public PgNormalDistribution newDistribution(double[] params) {
        return new PgNormalDistribution(params[0], params[1]);
    }

    @Override
    public PgNormalDistribution newDistribution(Sample sample) {
        return new PgNormalDistribution(sample.getMean(), sample.getStandardDeviation());
    }

    @Override
    public double[] getParamArray() {
        return new double[]{mean, standardDeviation};
    }

    @Override
    public Pair<double[]> bounds() {
        return new Pair<>(new double[]{Double.NEGATIVE_INFINITY, 1e-6},
                new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY});
    }

    @Override
    public String toString() {
        return "Normal(mean=" + String.format("%.2f", mean) + ", standardDeviation=" + String.format("%.2f", standardDeviation) + ")";
    }
}
