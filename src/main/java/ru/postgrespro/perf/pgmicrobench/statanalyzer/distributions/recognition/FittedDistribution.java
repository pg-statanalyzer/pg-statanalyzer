package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import lombok.Data;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;

/**
 * Represents a fitted distribution along with its parameters and p-value.
 */
@Data
public class FittedDistribution {
    public final double[] params;
    private final PgDistribution distribution;
    private final double pValue;
}
