package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;

public class SampleTarget<T extends PgDistribution> {
    public final T target;
    public final Sample sample;

    public SampleTarget(Sample sample, T target) {
        this.target = target;
        this.sample = sample;
    }
}