package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

public interface PgDistribution {
    int getParameterNumber();

    PgDistributionSample getSample(double[] params);
}
