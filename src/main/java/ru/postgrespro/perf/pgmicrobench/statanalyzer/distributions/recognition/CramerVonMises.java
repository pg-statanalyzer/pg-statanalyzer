package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.MaxEval;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

import java.util.Arrays;

/**
 * This class provides methods to perform the Cramer–Von Mises test for goodness of fit
 * and to fit a distribution to a given dataset using the Cramer–Von Mises statistic.
 */
public class CramerVonMises {

    /**
     * Calculates Cramer–Von Mises statistic for given data and distribution.
     *
     * @param data         sample data
     * @param distribution distribution to compare data against
     * @return value of Cramer–Von Mises statistic
     */
    public static double cvmStatistic(double[] data, PgDistribution distribution) {
        int n = data.length;
        Arrays.sort(data);

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double empiricalCdf = (2 * i + 1) / (2.0 * n);
            double theoreticalCdf = distribution.cdf(data[i]);
            double diff = empiricalCdf - theoreticalCdf;
            sum += diff * diff;
        }

        return (1.0 / (12.0 * n)) + sum;
    }

    /**
     * Calculates Cramer–Von Mises test statistic for given data and distribution.
     *
     * @param data         sample data
     * @param distribution distribution to compare data against
     * @return p-value for Cramer–Von Mises test
     */
    public static double cvmTest(double[] data, PgDistribution distribution) {
        return cvmTest(cvmStatistic(data, distribution), data.length);
    }

    /**
     * Calculates p-value for Cramer–Von Mises test based on computed statistic.
     *
     * @param statistic computed statistic for test
     * @param n         sample size
     * @return p-value for Cramer–Von Mises test
     */
    public static double cvmTest(double statistic, int n) {
        double lambda = statistic * (1 + 0.5 / n);
        double pValue = Math.exp(-lambda);
        return Math.min(pValue, 1.0);
    }

    /**
     * Fits distribution to data by minimizing Cramer–Von Mises statistic.
     *
     * @param data             sample data
     * @param startPoint       starting point for optimization process
     * @param distributionType type of distribution to fit to data
     * @return FittedDistribution object containing fitted parameters, distribution and p-value
     */
    public static FittedDistribution fit(double[] data, double[] startPoint, PgDistributionType distributionType) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = distributionType.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            return cvmStatistic(data, distribution);
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(evaluationFunction),
                GoalType.MINIMIZE,
                new InitialGuess(startPoint),
                new NelderMeadSimplex(startPoint.length)
        );

        double[] fittedParams = result.getPoint();
        double statistic = result.getValue();
        double pValue = cvmTest(statistic, data.length);

        PgDistribution fittedDistribution = distributionType.createDistribution(fittedParams);
        return new FittedDistribution(fittedParams, fittedDistribution, pValue);
    }
}
