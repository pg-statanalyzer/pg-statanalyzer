package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.Random;

/**
 * Represents a probability distribution.
 */
public interface PgDistribution {
    double pdf(double value);

    double cdf(double value);

    Sample generate(int size, Random random);

    PgDistributionType getType();

    int getParamNumber();

    PgDistribution newDistribution(double[] params);

    double[] getParamArray();

    Pair<double[]> bounds();
}
