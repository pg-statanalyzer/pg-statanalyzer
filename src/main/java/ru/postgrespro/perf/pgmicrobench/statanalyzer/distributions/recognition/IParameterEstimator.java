package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

public interface IParameterEstimator {
    FittedDistribution fit(Sample sample, PgDistributionType type);
}
