package ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.Well512a;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IDistributionTest;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.startpoint.LinearRegression;

import static org.apache.commons.math3.util.FastMath.log;

/**
 * Optimizer.
 */
public class PgOptimizer {
    private static final MaxEval MAX_EVAL = new MaxEval(10000);
    private static final CMAESOptimizer.PopulationSize POPULATION_SIZE = new CMAESOptimizer.PopulationSize(100);
    private static final double WEIGHT_STEP = 0.1;

    /**
     * Optimise composite distribution.
     *
     * @param sample sample.
     * @param distribution distribution.
     * @param statisticEvaluator statisticEvaluator.
     * @return parameters.
     */
    public static double[] optimize(Sample sample, PgCompositeDistribution distribution, IDistributionTest statisticEvaluator) {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
                10000,
                1e-4,
                true,
                1,
                5,
                new Well512a(42),
                false,
                null);
        MultivariateFunction evaluationFunction = point -> {
            PgCompositeDistribution d = distribution.newDistribution(point);

            double weightSum = 0;
            for (int i = 0; i < distribution.getSize(); i++) {
                weightSum += point[point.length - i - 1];
            }

            return statisticEvaluator.statistic(sample, d) + Math.pow(weightSum - 1, 2);
        };

        double[] startParam = distribution.getParamArray();
        double[] sigma = new double[distribution.getParamNumber()];
        int i = 0;
        for (; i < sigma.length - distribution.getSize(); i++) {
            sigma[i] = 0.3;
        }
        for (; i < sigma.length; i++) {
            sigma[i] = WEIGHT_STEP;
        }
        Pair<double[]> bounds = distribution.bounds();

        PointValuePair result = optimizer.optimize(
                MAX_EVAL,
                POPULATION_SIZE,
                GoalType.MINIMIZE,
                new ObjectiveFunction(evaluationFunction),
                new InitialGuess(startParam),
                new CMAESOptimizer.Sigma(sigma),
                new SimpleBounds(bounds.first, bounds.second));

        return result.getPoint();
    }

    /**
     * Optimise simple distribution.
     *
     * @param sample sample.
     * @param distribution distribution.
     * @param statisticEvaluator statisticEvaluator.
     * @return parameters.
     */
    public static double[] optimize(Sample sample, PgSimpleDistribution distribution, IDistributionTest statisticEvaluator) {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
                10000,
                1e-4,
                true,
                1,
                5,
                new Well512a(42),
                false,
                null);
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution d = distribution.newDistribution(point);
            return statisticEvaluator.statistic(sample, d);
        };

        double[] startParam = getStartingPoint(sample, distribution);
        double[] sigma = new double[distribution.getParamNumber()];
        for (int i = 0; i < sigma.length; i++) {
            sigma[i] = 1;
        }
        Pair<double[]> bounds = distribution.bounds();

        PointValuePair result = optimizer.optimize(
                MAX_EVAL,
                POPULATION_SIZE,
                GoalType.MINIMIZE,
                new ObjectiveFunction(evaluationFunction),
                new InitialGuess(startParam),
                new CMAESOptimizer.Sigma(sigma),
                new SimpleBounds(bounds.first, bounds.second)
        );

        return result.getPoint();
    }

    private static double[] getStartingPoint(Sample sample, PgSimpleDistribution distribution) {
        String resourcesFolder = "linearRegressionCoefs/";
        String distName = distribution.getType().name().toLowerCase();

        LinearRegression shapeModel = new LinearRegression(resourcesFolder + distName + "_param1_regression_params.json");
        LinearRegression scaleModel = new LinearRegression(resourcesFolder + distName + "_param2_regression_params.json");

        double logmean = log(sample.getMean());
        double logmedian = log(sample.getMedian());
        double logvariance = log(sample.getVariance());

        double shapeStartingPoint = shapeModel.predict(new double[]{logmean, logmedian, logvariance});
        double scaleStartingPoint = scaleModel.predict(new double[]{logmean, logmedian, logvariance});

        return new double[]{shapeStartingPoint, scaleStartingPoint};
    }
}
