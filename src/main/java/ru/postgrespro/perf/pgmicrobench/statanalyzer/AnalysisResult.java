package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.Data;

import java.util.List;
import java.util.function.Function;


/**
 * The AnalysisResult class encapsulates the results of a statistical
 * analysis performed on a dataset.
 */
@Data
public class AnalysisResult {
    final int modeNumber;
    final List<ModeReport> modeReports;
    final Function<Double, Double> pdf;
}