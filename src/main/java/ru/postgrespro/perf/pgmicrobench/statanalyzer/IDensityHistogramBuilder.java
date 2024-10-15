package ru.postgrespro.perf.pgmicrobench.statanalyzer;

public interface IDensityHistogramBuilder {
    DensityHistogram build(Sample sample, int binCount);
}
