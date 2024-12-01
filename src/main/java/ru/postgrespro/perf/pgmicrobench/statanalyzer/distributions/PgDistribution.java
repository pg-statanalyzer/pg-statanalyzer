package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.List;
import java.util.Random;

/**
 * Represents a probability distribution.
 */
public interface PgDistribution {
    double pdf(double value);

    double cdf(double value);

    double mean();

    double variance();

    double median();

    Sample generate(int size, Random random);

    PgDistributionType getType();
}
