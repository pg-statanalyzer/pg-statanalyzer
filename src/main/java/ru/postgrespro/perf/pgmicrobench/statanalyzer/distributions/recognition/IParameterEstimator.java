package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;

public interface IParameterEstimator {
    EstimatedParameters fit(Sample sample, PgSimpleDistribution type);

    EstimatedParameters fit(Sample sample, PgCompositeDistribution type);
}
