package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import java.util.List;
import java.util.Random;

public interface PgDistribution {
    double pdf(double value);

    double cdf(double value);

    List<Double> generate(int size, Random random);
}
