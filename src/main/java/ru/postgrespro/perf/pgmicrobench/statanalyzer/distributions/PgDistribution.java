package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.Random;

/**
 * Interface which represents a probability distribution.
 */
public interface PgDistribution {
    /**
     * Calculates the probability density function at given point.
     *
     * @param value the point at which to calculate PDF
     * @return value of PDF at the given point
     */
    double pdf(double value);

    /**
     * Calculates the cumulative distribution function at given point.
     *
     * @param value the point at which to calculate CDF
     * @return value of CDF at the given point
     */
    double cdf(double value);

    /**
     * Generates a sample from the implemented distribution.
     *
     * @param size required size of the sample
     * @param random Random object for generation
     * @return Sample object with generated sample
     */
    Sample generate(int size, Random random);

    /**
     * Returns the type of probability distribution.
     *
     * @return distribution type
     */
    PgDistributionType getType();

    /**
     * Returns the number of parameters needed for PDF calculation.
     *
     * @return number of parameters (in most cases it is 2)
     */
    int getParamNumber();

    /**
     * Creates a {@link PgDistribution} object based on given parameters.
     *
     * @param parameters array of parameters
     * @return distribution object with specified parameters
     */
    PgDistribution newDistribution(double[] parameters);

    /**
     * Creates a {@link PgDistribution} object with parameters estimated from sample.
     * It calculates estimated parameters of a distribution and uses them to create a new distribution instance.
     *
     * @param sample input sample
     * @return probability distribution with estimated parameters
     */
    PgDistribution newDistribution(Sample sample);

    /**
     * Returns the array of distribution's parameters.
     *
     * @return array of parameters
     */
    double[] getParamArray();

    /**
     * Returns the bounds of distribution parameters.
     *
     * @return pair of arrays (lower bounds, upper bounds)
     */
    Pair<double[]> bounds();
}