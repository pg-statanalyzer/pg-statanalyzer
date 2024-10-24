package ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.List;

public interface IQuantileEstimator {
    double[] quantiles(Sample sample, List<Double> probabilities);

    boolean supportsWeightedSamples();
}
