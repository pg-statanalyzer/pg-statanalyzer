package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.special.Gamma;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

import java.util.List;

import static org.apache.commons.math3.special.Gamma.logGamma;

/**
 * This class provides methods to perform the Cramer–Von Mises test for goodness of fit
 * and to fit a distribution to a given dataset using the Cramer–Von Mises statistic.
 */
public class CramerVonMises implements IDistributionTest, IParameterEstimator {

    /**
     * Calculates Cramer–Von Mises statistic for given data and distribution.
     *
     * @param sample       sample data
     * @param distribution distribution to compare data against
     * @return value of Cramer–Von Mises statistic
     */
    public static double cvmStatistic(Sample sample, PgDistribution distribution) {
        int n = sample.size();
        List<Double> sortedValues = sample.getSortedValues();

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double empiricalCdf = (2.0 * (i + 1) - 1) / (2.0 * n);
            double theoreticalCdf = distribution.cdf(sortedValues.get(i));
            double diff = theoreticalCdf - empiricalCdf;
            sum += diff * diff;
        }

        return (1.0 / (12.0 * n)) + sum;
    }

    /**
     * Computes p-value for given Cramer–Von Mises statistic.
     *
     * @param statistic Cramer–Von Mises statistic (must be positive)
     * @return p-value corresponding to statistic
     * @throws IllegalArgumentException if statistic is non-positive
     */
    private static double computePValue(double statistic) {
        if (statistic <= 0) {
            throw new IllegalArgumentException("Statistic must be positive.");
        }
        return 1.0 - computeA1Function(statistic);
    }

    /**
     * Computes A1(s) function used in p-value calculation for CVM statistic.
     *
     * @param s input parameter (must be positive)
     * @return value of A1(s) function
     * @throws IllegalArgumentException if input parameter is non-positive
     */
    private static double computeA1Function(double s) {
        if (s <= 0) {
            throw new IllegalArgumentException("Statistic must be positive.");
        }

        double sum = 0.0;
        double sqrt2s = Math.sqrt(2 * s);

        for (int j = 0; j < 1000; j++) {
            double term = Math.pow(4 * j + 1, 2) / (16.0 * s);

            if (term > 700) {
                break;
            }

            double expTerm = Math.exp(-term);
            double bessel1 = modifiedBessel(-0.25, term);
            double bessel2 = modifiedBessel(0.25, term);

            if (Double.isNaN(bessel1) || Double.isNaN(bessel2)) {
                throw new IllegalStateException("NaN in Bessel function at j = " + j);
            }

            double gammaRatio = safeGammaRatio(j + 0.5, j + 1);
            double factor = Math.sqrt(4 * j + 1) * gammaRatio * expTerm * (bessel1 - bessel2);

            sum += factor;

            if (Math.abs(factor / sum) < 1e-8) {
                break;
            }
        }

        return sum / sqrt2s;
    }

    /**
     * Computes modified Bessel function of first kind (I_v(z)) for given order and argument.
     *
     * @param v order of Bessel function (can be negative or positive)
     * @param z argument of Bessel function (must be non-negative)
     * @return value of I_v(z)
     * @throws IllegalArgumentException if z is negative
     */
    private static double modifiedBessel(double v, double z) {
        if (z < 0) {
            throw new IllegalArgumentException("z must be non-negative.");
        }

        double sum = 0.0;
        double step = 2 * Math.log(z / 2.0);
        double c = v * Math.log(z / 2.0);
        for (int k = 0; k < 1000; k++) {
            double term = Math.exp(c - Gamma.logGamma(k + 1) - logGamma(k + v + 1));
            sum += term;

            if (term < 1e-12) {
                break;
            }

            c += step;
        }

        return sum;
    }

    /**
     * Safely computes ratio of two gamma functions: Gamma(x_1) / Gamma(x_2).
     *
     * @param x1 first argument of gamma function
     * @param x2 second argument of gamma function
     * @return ratio Gamma(x_1) / Gamma(x_2), computed via logarithms for numerical stability
     */
    private static double safeGammaRatio(double x1, double x2) {
        double logGammaDiff = Gamma.logGamma(x1) - Gamma.logGamma(x2);
        return Math.exp(logGammaDiff) / Math.sqrt(Math.PI);
    }

    /**
     * Computes p-value for CVM test given dataset and theoretical distribution.
     *
     * @param sample       dataset as array of doubles
     * @param distribution theoretical distribution, providing CDF implementation
     * @return p-value for CVM test
     */
    @Override
    public double test(Sample sample, PgDistribution distribution) {
        double statistic = cvmStatistic(sample, distribution);
        return computePValue(statistic);
    }

    /**
     * Fits distribution to data by minimizing Cramer–Von Mises statistic.
     *
     * @param sample           sample data
     * @param distributionType type of distribution to fit to data
     * @return EstimatedParameters object containing fitted parameters, distribution and p-value
     */
    @Override
    public EstimatedParameters fit(Sample sample, PgDistributionType distributionType) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = distributionType.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            return cvmStatistic(sample, distribution);
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(evaluationFunction),
                GoalType.MINIMIZE,
                new InitialGuess(distributionType.getStartPoint()),
                new NelderMeadSimplex(distributionType.getParameterNumber())
        );

        double[] fittedParams = result.getPoint();
        double statistic = result.getValue();
        double pValue = computePValue(statistic);

        PgDistribution fittedDistribution = distributionType.createDistribution(fittedParams);
        return new EstimatedParameters(fittedParams, fittedDistribution, pValue);
    }
}
