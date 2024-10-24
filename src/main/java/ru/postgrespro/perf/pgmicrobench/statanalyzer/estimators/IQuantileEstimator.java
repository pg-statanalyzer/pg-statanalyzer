package ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.List;


/**
 * Interface for quantile estimation methods.
 * Implementations of this interface provide ways to estimate quantiles
 * from given sample and list of probabilities.
 */

public interface IQuantileEstimator {

    /**
     * Estimates quantiles for provided sample based on given probabilities.
     * Each probability corresponds to specific quantile value.
     *
     * @param sample        sample data for which quantiles are to be estimated.
     * @param probabilities list of probabilities (values between 0 and 1)
     *                      indicating quantiles to estimate.
     * @return array of quantile estimates corresponding to input probabilities.
     */
    double[] quantiles(Sample sample, List<Double> probabilities);

    /**
     * Indicates whether quantile estimator supports weighted samples.
     * Some estimators may take into account weights associated with sample elements.
     *
     * @return {@code true} if estimator supports weighted samples, {@code false} otherwise.
     */
    boolean supportsWeightedSamples();
}
