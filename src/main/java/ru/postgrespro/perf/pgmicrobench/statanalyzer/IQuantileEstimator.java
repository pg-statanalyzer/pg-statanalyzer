package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.util.List;

public interface IQuantileEstimator {
    double[] quantiles(Sample sample, List<Double> probabilities);

    boolean supportsWeightedSamples();
}
