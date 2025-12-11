package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;

public interface IParameterEstimator {
    /**
     * Estimates parameters of presumed distribution.
     *
     * @param sample Sample
     * @param type the type of distribution to fit
     * @return Estimated distribution with fitted parameters and p-value
     */
    EstimatedParameters fit(Sample sample, PgSimpleDistribution type);

    /**
     * Estimates parameters for presumed composite distribution (union of simple distributions).
     *
     * @param sample Sample
     * @param type the type of composite distribution, which contains list of simple distributions
     * @return Estimated distribution with fitted parameters and p-value
     */
    EstimatedParameters fit(Sample sample, PgCompositeDistribution type);
}
