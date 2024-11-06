package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import static ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Pearson.pearsonFitImplementation;

/**
 * The PGLogNormalDistribution class implements log-normal distribution.
 */
public class PgLogNormalDistribution {
    private static final int PARAMETER_NUMBER = 2;

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
            return new LogNormalDistribution(params[0], params[1]);
        }));
    }
}
