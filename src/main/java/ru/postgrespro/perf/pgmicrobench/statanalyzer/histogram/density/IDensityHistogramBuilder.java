package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

public interface IDensityHistogramBuilder {
    DensityHistogram build(Sample sample, int binCount);
}
