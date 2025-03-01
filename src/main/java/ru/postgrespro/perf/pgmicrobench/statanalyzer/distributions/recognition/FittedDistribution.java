package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import lombok.Data;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

/**
 * The FittedDistribution class represents a probability distribution
 * that has been fitted to a dataset.
 */
@Data
public class FittedDistribution {
    final PgDistribution distribution;
    final double pValue;
}
