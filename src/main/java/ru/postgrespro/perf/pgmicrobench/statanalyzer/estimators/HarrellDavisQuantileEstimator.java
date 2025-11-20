package ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.math3.distribution.BetaDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.WeightedSample;

import java.util.List;
import java.util.Objects;


/**
 * Implements Harrell-Davis quantile estimator.
 * This estimator provides quantile estimates based on weighted sample moments.
 * We recommend reading original article from A. Akinshin:
 * <a href="https://aakinshin.net/posts/qrde-hd/">Quantile-respectful density estimation based on the Harrell-Davis quantile estimator</a>
 */

public class HarrellDavisQuantileEstimator implements IQuantileEstimator {

    private static final HarrellDavisQuantileEstimator INSTANCE;

    static {
        INSTANCE = new HarrellDavisQuantileEstimator();
    }

    private HarrellDavisQuantileEstimator() {
    }

    /**
     * Returns singleton instance of this estimator.
     *
     * @return singleton instance of {@code HarrellDavisQuantileEstimator}.
     */
    public static HarrellDavisQuantileEstimator getInstance() {
        return INSTANCE;
    }

    /**
     * Estimates quantiles for given sample and list of probabilities.
     *
     * @param sample        sample data for which quantiles are to be estimated.
     * @param probabilities list of probabilities for which quantiles are calculated.
     * @return array of quantile estimates corresponding to input probabilities.
     */
    @Override
    public double[] quantiles(WeightedSample sample, List<Double> probabilities) {
        double[] result = new double[probabilities.size()];
        for (int i = 0; i < probabilities.size(); i++) {
            result[i] = getMoment(sample, probabilities.get(i), false).getC1();
        }
        return result;
    }

    /**
     * Indicates whether estimator supports weighted samples.
     *
     * @return {@code true} since this estimator supports weighted samples.
     */
    @Override
    public boolean supportsWeightedSamples() {
        return true;
    }

    /**
     * Computes Harrell-Davis quantile estimate using beta-distribution based weighting.
     * Weights order statistics using differences in Beta((n+1)p, (n+1)(1-p)) CDF values.
     *
     * @param sample weighted sample data
     * @param probability quantile probability level
     * @param calcSecondMoment whether to compute second moment for variance estimation
     * @return Moments containing quantile estimate (c1) and optionally second moment (c2)
     */
    private Moments getMoment(WeightedSample sample, double probability, boolean calcSecondMoment) {
        int n = sample.size();
        double a = (n + 1) * probability;
        double b = (n + 1) * (1 - probability);
        BetaDistribution betaDistribution = new BetaDistribution(a, b);

        double c1 = 0;
        double c2 = calcSecondMoment ? 0 : Double.NaN;
        double betaCdfRight = 0;
        double currentProbability = 0;

        List<Double> sortedValues = sample.getSortedValues();
        List<Double> sortedWeights = sample.getSortedWeights();

        for (int j = 0; j < n; j++) {
            double betaCdfLeft = betaCdfRight;
            currentProbability += sortedWeights.get(j);

            betaCdfRight = betaDistribution.cumulativeProbability(currentProbability);
            double w = betaCdfRight - betaCdfLeft;

            c1 += w * sortedValues.get(j);
            if (calcSecondMoment) {
                c2 += w * Math.pow(sortedValues.get(j), 2);
            }
        }

        return new Moments(c1, c2);
    }

    @Getter
    @ToString
    @RequiredArgsConstructor
    private static final class Moments {
        private final double c1;
        private final double c2;

        @Override
        public int hashCode() {
            return Objects.hash(c1, c2);
        }
    }
}
