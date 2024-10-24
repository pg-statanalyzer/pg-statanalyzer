package ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import org.apache.commons.math3.distribution.BetaDistribution;
import java.util.List;
import java.util.Objects;


/**
 * Implements Harrell-Davis quantile estimator.
 * This estimator provides quantile estimates based on weighted sample moments.
 */

public class HarrellDavisQuantileEstimator implements IQuantileEstimator {
    private static final HarrellDavisQuantileEstimator INSTANCE = new HarrellDavisQuantileEstimator();

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
    public double[] quantiles(Sample sample, List<Double> probabilities) {
        double[] result = new double[probabilities.size()];
        for (int i = 0; i < probabilities.size(); i++) {
            result[i] = getMoment(sample, probabilities.get(i), false).c1;
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

    private Moments getMoment(Sample sample, double probability, boolean calcSecondMoment) {
        int n = sample.getSize();
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
            currentProbability += sample.isWeighted
                    ? sortedWeights.get(j) / sample.getTotalWeight()
                    : 1.0 / n;

            betaCdfRight = betaDistribution.cumulativeProbability(currentProbability);
            double w = betaCdfRight - betaCdfLeft;

            c1 += w * sortedValues.get(j);
            if (calcSecondMoment) {
                c2 += w * Math.pow(sortedValues.get(j), 2);
            }
        }

        return new Moments(c1, c2);
    }

    private static final class Moments {
        private final double c1;
        private final double c2;

        private Moments(double c1, double c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(c1, c2);
        }

        @Override
        public String toString() {
            return "Moments[" +
                    "c1=" + c1 + ", " +
                    "c2=" + c2 + ']';
        }

    }
}
