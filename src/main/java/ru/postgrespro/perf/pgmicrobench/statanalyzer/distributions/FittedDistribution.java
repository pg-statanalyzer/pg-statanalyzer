package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import lombok.Data;

/**
 * Represents a fitted distribution along with its parameters and p-value.
 */
@Data
public class FittedDistribution {
    public final double[] params;
    private final PgDistribution distribution;
    private final double pValue;
}
