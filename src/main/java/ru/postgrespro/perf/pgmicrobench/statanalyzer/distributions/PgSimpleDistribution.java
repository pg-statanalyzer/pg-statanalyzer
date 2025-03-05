package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

/**
 * Simple distribution.
 */
public interface PgSimpleDistribution extends PgDistribution {
    double mean();

    double variance();

    double median();

    double skewness();

    double kurtosis();

    default double standardDeviation() {
        return Math.sqrt(variance());
    }
}
