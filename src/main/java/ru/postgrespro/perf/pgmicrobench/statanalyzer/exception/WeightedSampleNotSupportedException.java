package ru.postgrespro.perf.pgmicrobench.statanalyzer.exceptions;


/**
 * Exception thrown when operation is attempted on quantile estimator
 * that does not support weighted samples.
 */

public class WeightedSampleNotSupportedException extends RuntimeException {

    /**
     * Constructs {@code WeightedSampleNotSupportedException} with default message
     * indicating that weighted samples are not supported.
     */
    public WeightedSampleNotSupportedException() {
        super("Weighted samples are not supported by this quantile estimator.");
    }
}
