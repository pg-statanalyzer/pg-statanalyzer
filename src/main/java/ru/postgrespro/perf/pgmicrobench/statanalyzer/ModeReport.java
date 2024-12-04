package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.Data;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;

import java.util.List;

/**
 * The ModeReport class encapsulates the results of the analysis for a
 * specific mode detected in a dataset.
 */
@Data
public class ModeReport {
    final long size;
    final double location;
    final double leftBound;
    final double rightBound;
    final FittedDistribution bestDistribution;
    final List<FittedDistribution> fittedDistributions;
}
