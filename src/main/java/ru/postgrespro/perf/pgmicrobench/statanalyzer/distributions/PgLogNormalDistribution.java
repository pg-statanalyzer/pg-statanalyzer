package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Pearson.pearsonFitImplementation;

/**
 * The PGLogNormalDistribution class implements log-normal distribution.
 */
public class PgLogNormalDistribution implements PgDistribution {
    private static final int PARAMETER_NUMBER = 2;

    private final double mean;
    private final double standardDeviation;

    public PgLogNormalDistribution(double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation must be greater than zero");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Fits a log-normal distribution to the provided data using the Pearson fitting method.
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
            return new PgLogNormalDistribution(params[0], params[1]);
        }));
    }


    @Override
    public double pdf(double value) {
        if (value <= 0) {
            return 0;
        }
        double logValue = Math.log(value);
        return (1 / (value * standardDeviation * Math.sqrt(2 * Math.PI))) *
                Math.exp(-Math.pow(logValue - mean, 2) / (2 * standardDeviation * standardDeviation));
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
    public List<Double> generate(int size, Random random) {
        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double sample = Math.exp(mean + standardDeviation * random.nextGaussian());
            samples.add(sample);
        }
        return samples;
    }
}
