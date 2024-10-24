package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;


/**
 * Interface for building density histogram from sample.
 * Implementations of this interface define how to construct histogram with specified number of bins.
 */

public interface IDensityHistogramBuilder {

    /**
     * Builds density histogram from provided sample data.
     *
     * @param sample   sample data from which histogram is to be built.
     * @param binCount number of bins in histogram. Must be positive integer.
     * @return {@code DensityHistogram} representing data distribution of sample.
     * @throws IllegalArgumentException if {@code binCount} is not positive integer.
     */
    DensityHistogram build(Sample sample, int binCount);
}
