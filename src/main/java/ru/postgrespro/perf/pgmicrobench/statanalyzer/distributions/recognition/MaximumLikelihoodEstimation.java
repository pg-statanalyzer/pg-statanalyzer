package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer.PgOptimizer;

/**
 * Class for estimating distribution parameters using the Maximum Likelihood Estimation (MLE) method.
 */
public class MaximumLikelihoodEstimation implements IParameterEstimator {
    private static final IStatisticEvaluator statisticEvaluatorMLE = (sample, distribution) ->
            sample.getValues().stream().mapToDouble((x) -> -Math.log(distribution.pdf(x))).sum();

    /**
     * Estimates the parameters of a given distribution using the maximum likelihood estimation method.
     *
     * @param sample           an array of observed data for which the distribution parameters need to be estimated
     * @param distribution the type of distribution to be fitted to the data
     * @return a EstimatedParameters object containing the estimated parameters and the corresponding distribution
     */
    // TODO тут могут быть проблемы, нодо потестить
    @Override
    public EstimatedParameters fit(Sample sample, PgSimpleDistribution distribution) {
        double[] solution = PgOptimizer.optimize(sample, distribution, statisticEvaluatorMLE);

        PgDistribution optimizedDist = distribution.newDistribution(solution);
        double pValue = Math.exp(-statisticEvaluatorMLE.statistic(sample, optimizedDist));

        return new EstimatedParameters(optimizedDist, pValue);
    }

    @Override
    public EstimatedParameters fit(Sample sample, PgCompositeDistribution distribution) {
        double[] solution = PgOptimizer.optimize(sample, distribution, statisticEvaluatorMLE);

        PgCompositeDistribution optimizedDist = distribution.newDistribution(solution);
        double pValue = Math.exp(-statisticEvaluatorMLE.statistic(sample, optimizedDist));

        return new EstimatedParameters(optimizedDist, pValue);
    }
}
