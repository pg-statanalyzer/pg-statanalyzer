package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import lombok.Data;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

import java.util.Arrays;

/**
 * The {@code Pearson} class provides implementation of Pearson's algorithm for fitting distributions
 * and performing the Pearson goodness-of-fit test.
 */
public class Pearson {
    private static final int BINS = 50;

    /**
     * Fits a distribution to the observed data by minimizing the Pearson statistic.
     *
     * @param data             the observed data
     * @param startPoint       initial parameter guess for the distribution
     * @param distributionType the type of distribution to fit
     * @return a FittedDistribution object with fitted parameters, sample, and p-value
     */
    public static FittedDistribution fit(double[] data, double[] startPoint, PgDistributionType distributionType) {
        Bounds bounds = boundsOfBins(data);
        int actualBins = bounds.counts.length;

        double[] observed = new double[actualBins];
        for (int i = 0; i < actualBins; i++) {
            observed[i] = (double) bounds.counts[i] / data.length;
        }

        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = distributionType.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            double[] expected = new double[actualBins];
            double prev = 0;
            for (int i = 0; i < actualBins - 1; i++) {
                double cur = distribution.cdf(bounds.bounds[i + 1]);
                expected[i] = cur - prev;
                prev = cur;
            }
            expected[actualBins - 1] = 1 - prev;

            return statistic(expected, observed, data.length);
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
        double statistic = result.getValue();
        double pValue = pearsonTest(statistic, actualBins, distributionType.getParameterNumber());

        return new FittedDistribution(solution, distributionType.createDistribution(solution), pValue);
    }

    /**
     * Calculates the p-value for a given Pearson statistic, number of bins, and degrees of freedom.
     *
     * @param statistic       the Pearson statistic
     * @param bins            the number of bins used in the test
     * @param degreeOfFreedom the degrees of freedom for the distribution
     * @return the p-value corresponding to the statistic
     */
    public static double pearsonTest(double statistic, int bins, int degreeOfFreedom) {
        return 1 - new ChiSquaredDistribution(bins - 1 - degreeOfFreedom).cumulativeProbability(statistic);
    }

    /**
     * Performs the Pearson goodness-of-fit test on the given data against a specified distribution.
     *
     * @param data         the observed data
     * @param distribution the theoretical distribution to compare against
     * @return the p-value of the Pearson test
     */
    public static double pearsonTest(double[] data, PgDistribution distribution) {
        Bounds bounds = boundsOfBins(data);
        int actualBins = bounds.counts.length;

        double[] observed = new double[actualBins];
        for (int i = 0; i < actualBins; i++) {
            observed[i] = (double) bounds.counts[i] / data.length;
        }

        double[] expected = new double[actualBins];
        double prev = 0;
        for (int i = 0; i < actualBins - 1; i++) {
            double cur = distribution.cdf(bounds.bounds[i + 1]);
            expected[i] = cur - prev;
            prev = cur;
        }
        expected[actualBins - 1] = 1 - prev;

        double statistic = statistic(observed, expected, data.length);

        return pearsonTest(statistic, actualBins, distribution.getType().getParameterNumber());
    }

    private static double statistic(double[] observed, double[] expected, int n) {
        if (observed.length != expected.length) {
            throw new IllegalArgumentException("observed.length != expected.length");
        }

        double statistic = 0.0;
        for (int i = 0; i < observed.length; i++) {
            statistic += observed[i] * observed[i] / expected[i];
        }
        statistic -= 1;
        return statistic * n;
    }

    private static Bounds boundsOfBins(double[] arr) {
        double min = Arrays.stream(arr).min().orElse(Double.NaN);
        double max = Arrays.stream(arr).max().orElse(Double.NaN);
        double range = max - min;

        double binWidth = range / BINS;

        int[] counts = new int[BINS];
        double[] bounds = new double[BINS + 1];

        for (int i = 0; i < BINS + 1; i++) {
            bounds[i] = min + i * binWidth;
        }

        for (double value : arr) {
            int binIndex = (int) ((value - min) / binWidth);
            if (binIndex >= BINS) {
                binIndex = BINS - 1;
            }
            counts[binIndex]++;
        }

        int[] mergedCounts = new int[BINS];
        double[] mergedBounds = new double[BINS + 1];
        mergedCounts[0] = counts[0];
        mergedBounds[0] = bounds[0];
        mergedBounds[1] = bounds[1];
        int mergedIndex = 0;

        for (int i = 1; i < BINS; i++) {
            if (counts[i] < 1) {
                mergedCounts[mergedIndex] += counts[i];
            } else {
                mergedIndex++;
                mergedCounts[mergedIndex] = counts[i];
            }
            mergedBounds[mergedIndex + 1] = bounds[i + 1];
        }
        mergedIndex++;

        int[] resultCounts = new int[mergedIndex];
        double[] resultBounds = new double[mergedIndex + 1];

        System.arraycopy(mergedCounts, 0, resultCounts, 0, mergedIndex);
        System.arraycopy(mergedBounds, 0, resultBounds, 0, mergedIndex);

        return new Bounds(resultCounts, resultBounds);
    }

    @Data
    private static class Bounds {
        private final int[] counts;
        private final double[] bounds;
    }
}
