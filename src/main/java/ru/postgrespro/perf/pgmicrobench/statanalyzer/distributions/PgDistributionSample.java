package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import java.util.List;
import java.util.Random;

/**
 * Represents a probability distribution.
 */
public interface PgDistributionSample {
    double pdf(double value);

    double cdf(double value);

    double mean();

    double variance();

    double median();

    List<Double> generate(int size, Random random);
}
