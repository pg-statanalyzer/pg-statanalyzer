package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;

public interface IDistributionTest {
    public double statistic(Sample sample, PgDistribution distribution);

    double test(Sample sample, PgDistribution distribution);
}
