package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.distribution.NormalDistribution;

import static ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Pearson.pearsonFitImplementation;

/**
 * The PGLogNormalDistribution class implements normal distribution.
 */
public class PGNormalDistribution {
    private static final int PARAMETER_NUMBER = 2;

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
            return new NormalDistribution(params[0], params[1]);
        }));
    }
}
