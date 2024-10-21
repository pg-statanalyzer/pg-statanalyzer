package ru.postgrespro.perf.pgmicrobench.statanalyzer.Exceptions;

public class WeightedSampleNotSupportedException extends RuntimeException {
    public WeightedSampleNotSupportedException() {
        super("Weighted samples are not supported by this quantile estimator.");
    }
}
