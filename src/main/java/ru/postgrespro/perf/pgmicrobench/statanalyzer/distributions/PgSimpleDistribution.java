package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

/**
 * Simple distribution.
 */
public interface PgSimpleDistribution extends PgDistribution {
    double mean();

    double variance();

    double median();

    double skewness();

    double kurtosis();

    @Override
    PgSimpleDistribution newDistribution(Sample sample);

    default double standardDeviation() {
        return Math.sqrt(variance());
    }
}
