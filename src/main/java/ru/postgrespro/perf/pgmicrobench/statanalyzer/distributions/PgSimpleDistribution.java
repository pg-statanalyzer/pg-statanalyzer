package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

/**
 * Simple distribution.
 */
public interface PgSimpleDistribution extends PgDistribution {
    /**
     * Returns the mean (expected value) of the distribution.
     * @return mean of the distribution
     */
    double mean();

    /**
     * Returns the variance of the distribution.
     * @return variance of the distribution
     */
    double variance();

    /**
     * Returns the median (50th percentile) of the distribution
     * @return median of the distribution
     */
    double median();

    /**
     * Returns the skewness (measure of asymmetry) of the distribution
     * @return skewness of the distribution
     */
    double skewness();

    /**
     * Returns the kurtosis (measure of tail heaviness) of the distribution
     * @return kurtosis of the distribution
     */
    double kurtosis();

    @Override
    PgSimpleDistribution newDistribution(Sample sample);

    /**
     * Returns the standard deviation of the distribution.
     * @return standard deviation of the distribution
     */
    default double standardDeviation() {
        return Math.sqrt(variance());
    }
}