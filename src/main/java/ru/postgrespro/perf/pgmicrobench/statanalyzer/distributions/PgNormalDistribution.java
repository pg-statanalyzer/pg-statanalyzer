package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Pearson.pearsonFitImplementation;

/**
 * The PgNormalDistribution class implements normal distribution.
 */
public class PgNormalDistribution implements PgDistribution {
    private static final int PARAMETER_NUMBER = 2;

    private final double mean;
    private final double standardDeviation;

    /**
     * Constructor.
     */
    public PgNormalDistribution(double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation must be greater than zero");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Fits a normal distribution to the provided data using the Pearson fitting method.
     *
     * @param data       an array of double values representing the dataset to fit.
     * @param startPoint an array of double values representing the initial guess for the parameters.
     * @return a FittedDistribution object representing the fitted log-normal distribution.
     */
    public static FittedDistribution pearsonFit(double[] data, double[] startPoint) {
        return pearsonFitImplementation(data, startPoint, PARAMETER_NUMBER, (params -> {
            if (params[1] <= 0) {
                throw new IllegalArgumentException("Wrong number of parameters");
            }
            return new PgNormalDistribution(params[0], params[1]);
        }));
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
    public List<Double> generate(int size, Random random) {
        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double sample = mean + standardDeviation * random.nextGaussian();
            samples.add(sample);
        }
        return samples;
    }
}
