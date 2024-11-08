package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.FittedDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;

import java.util.Arrays;
import java.util.function.Function;

/**
 * The {@code Pearson} class provides implementation of Pearson's algorithm.
 */
public class Pearson {
    /**
     * Fits a distribution to the provided data using Pearson's method.
     *
     * @param data            An array of double values representing the dataset to be fitted.
     * @param startPoint      An array of double values representing the initial guess for the
     *                        parameters of the distribution.
     * @param degreeOfFreedom The degrees of freedom for the fitted distribution.
     * @param getDistribution A function that takes an array of parameters and returns a
     *                        {@link PgDistribution} representing the distribution to be fitted.
     * @return A {@link FittedDistribution} object containing the fitted distribution, the
     * parameters of the fitted distribution, and the p-value of the fit.
     */
    public static FittedDistribution pearsonFitImplementation(double[] data, double[] startPoint,
                                                              int degreeOfFreedom, Function<double[], PgDistribution> getDistribution) {
        int bins = (int) Math.sqrt(data.length) + 1;

        double[] bounds = boundsOfBins(data, bins);

        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = getDistribution.apply(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            double[] theoreticalFreq = new double[bins];
            double prev = 0;
            for (int i = 0; i < bins - 1; i++) {
                double cur = distribution.cdf(bounds[i]);
                theoreticalFreq[i] = cur - prev;
                prev = cur;
            }
            theoreticalFreq[bins - 1] = 1 - prev;

            double sum = 0;
            for (int i = 0; i < bins; i++) {
                sum += 1.0 / theoreticalFreq[i];
            }

            return sum;
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(evaluationFunction),
                GoalType.MINIMIZE,
                new InitialGuess(startPoint),
                new NelderMeadSimplex(2)
        );

        double[] solution = result.getPoint();
        double functionValue = result.getValue();

        double statistic = functionValue - bins * bins;
        statistic /= (bins * bins);
        statistic *= data.length;

        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(bins - 1 - degreeOfFreedom);
        double pValue = 1 - chiSquaredDistribution.cumulativeProbability(statistic);

        return new FittedDistribution(solution, getDistribution.apply(solution), pValue);
    }


    // TODO change splitting method

    /**
     * Calculates the bounds of the bins for the histogram based on the provided data.
     *
     * @param arr  An array of double values representing the dataset.
     * @param bins The number of bins to be created.
     * @return An array of double values representing the upper bounds of the bins.
     */
    private static double[] boundsOfBins(double[] arr, int bins) {
        int dataSize = arr.length;

        double[] sorted = Arrays.stream(arr).sorted().toArray();

        double[] bounds = new double[bins - 1];

        int amount = dataSize / bins;
        int rem = dataSize - amount * bins;
        int curPos = 0;

        for (int i = 0; i < bounds.length; i++) {
            curPos += amount + (i < rem ? 1 : 0);
            bounds[i] = sorted[curPos - 1];
        }

        return bounds;
    }
}
