package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

import java.util.Arrays;

/**
 * This class provides methods to perform the Kolmogorov-Smirnov test for goodness of fit
 * and to fit a distribution to a given dataset using the Kolmogorov-Smirnov statistic.
 */
public class KolmogorovSmirnov {
    private static final KolmogorovSmirnovTest KS_TEST = new KolmogorovSmirnovTest();

    /**
     * Calculates the Kolmogorov-Smirnov statistic for the given data and distribution.
     *
     * @param data the observed data
     * @param distribution the theoretical distribution to compare against
     * @return the Kolmogorov-Smirnov statistic
     */
    public static double ksStatistic(double[] data, PgDistribution distribution) {
        int n = data.length;
        double nd = n;
        double[] dataCopy = new double[n];
        System.arraycopy(data, 0, dataCopy, 0, n);
        Arrays.sort(dataCopy);
        double d = 0.0;

        for (int i = 1; i <= n; ++i) {
            double yi = distribution.cdf(dataCopy[i - 1]);
            double currD = Math.max(i / nd - yi, yi - (i - 1) / nd);
            if (currD > d) {
                d = currD;
            }
        }

        return d;
    }

    /**
     * Performs the Kolmogorov-Smirnov test on the given data against a specified distribution.
     *
     * @param data the observed data
     * @param distribution the theoretical distribution to compare against
     * @return the p-value of the Kolmogorov-Smirnov test
     */
    public static double ksTest(double[] data, PgDistribution distribution) {
        return ksTest(ksStatistic(data, distribution), data.length);
    }

    /**
     * Calculates the p-value for a given Kolmogorov-Smirnov statistic and sample size.
     *
     * @param statistic the Kolmogorov-Smirnov statistic
     * @param n the sample size
     * @return the p-value corresponding to the statistic
     */
    public static double ksTest(double statistic, int n) {
        return 1.0 - KS_TEST.cdf(statistic, n);
    }

    /**
     * Fits a distribution to the observed data by minimizing the Kolmogorov-Smirnov statistic.
     *
     * @param data the observed data
     * @param startPoint initial parameter guess for the distribution
     * @param distributionType the type of distribution to fit
     * @return a FittedDistribution object with fitted parameters, sample, and p-value
     */
    public static FittedDistribution fit(double[] data, double[] startPoint, PgDistributionType distributionType) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = distributionType.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            return ksStatistic(data, distribution);
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
        double pValue = ksTest(statistic, data.length);

        return new FittedDistribution(solution, distributionType.createDistribution(solution), pValue);
    }
}
