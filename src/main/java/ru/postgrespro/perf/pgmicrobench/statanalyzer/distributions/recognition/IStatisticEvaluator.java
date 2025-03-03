package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;

/**
 * IStatisticEvaluator.
 */
public interface IStatisticEvaluator {
    double statistic(Sample sample, PgDistribution distribution);
}
