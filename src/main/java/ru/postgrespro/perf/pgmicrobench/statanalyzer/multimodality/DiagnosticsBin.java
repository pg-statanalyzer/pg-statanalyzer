package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


/**
 * Class representing diagnostic bin for analyzing density histograms.
 * Each {@code DiagnosticsBin} wraps {@link DensityHistogramBin} and tracks additional
 * diagnostic properties such as water level, mode, lowland and peak status.
 */

@Data
@AllArgsConstructor
public class DiagnosticsBin {

    /**
     * Underlying histogram bin associated with this diagnostics bin.
     */
    private final DensityHistogramBin histogramBin;

    /**
     * Water level associated with this bin.
     * Defaults to height of corresponding histogram bin.
     */
    @Getter
    @Setter
    private double waterLevel;

    /**
     * Flag indicating whether this bin represents mode.
     */
    @Getter
    @Setter
    private boolean isMode;

    /**
     * Flag indicating whether this bin is considered lowland.
     */
    @Getter
    @Setter
    private boolean isLowland;

    /**
     * Flag indicating whether this bin is identified as peak.
     */
    @Getter
    @Setter
    private boolean isPeak;

    /**
     * Constructs {@code DiagnosticsBin} with given histogram bin.
     * Water level is initialized to height of histogram bin.
     * All flags (mode, lowland, peak) are set to {@code false} by default.
     *
     * @param histogramBin {@link DensityHistogramBin} associated with this bin.
     */
    public DiagnosticsBin(DensityHistogramBin histogramBin) {
        this.histogramBin = histogramBin;
        this.waterLevel = histogramBin.getHeight();
        this.isMode = false;
        this.isLowland = false;
        this.isPeak = false;
    }
}
