package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

/**
 * Class for estimating distribution parameters using the Maximum Likelihood Estimation (MLE) method.
 */
public class MaximumLikelihoodEstimation implements IParameterEstimator {

    /**
     * Estimates the parameters of a given distribution using the maximum likelihood estimation method.
     *
     * @param sample           an array of observed data for which the distribution parameters need to be estimated
     * @param distributionType the type of distribution to be fitted to the data
     * @return a FittedDistribution object containing the estimated parameters and the corresponding distribution
     */
    @Override
    public FittedDistribution fit(Sample sample, PgDistributionType distributionType) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = distributionType.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            double sum = 0;

            for (double datum : sample) {
                sum -= Math.log(distribution.pdf(datum));
            }

            return sum;
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(evaluationFunction),
                GoalType.MINIMIZE,
                new InitialGuess(distributionType.getStartPoint()),
                new NelderMeadSimplex(distributionType.getParameterNumber())
        );

        double[] solution = result.getPoint();
        double pValue = Math.exp(-result.getValue());

        return new FittedDistribution(solution, distributionType.createDistribution(solution), pValue);
    }
}
