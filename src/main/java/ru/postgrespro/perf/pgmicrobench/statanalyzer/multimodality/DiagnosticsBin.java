package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;


/**
 * Class representing diagnostic bin for analyzing density histograms.
 * Each {@code DiagnosticsBin} wraps {@link DensityHistogramBin} and tracks additional
 * diagnostic properties such as water level, mode, lowland and peak status.
 */

public class DiagnosticsBin {
    private final DensityHistogramBin histogramBin;
    private double waterLevel;
    private boolean isMode;
    private boolean isLowland;
    private boolean isPeak;

    public DiagnosticsBin(DensityHistogramBin histogramBin) {
        this.histogramBin = histogramBin;
        this.waterLevel = histogramBin.height();
        this.isMode = false;
        this.isLowland = false;
        this.isPeak = false;
    }

    public DensityHistogramBin getHistogramBin() {
        return histogramBin;
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
    }

    public boolean getIsMode() {
        return isMode;
    }

    public void setIsMode(boolean isMode) {
        this.isMode = isMode;
    }

    public boolean getIsLowland() {
        return isLowland;
    }

    public void setIsLowland(boolean isLowland) {
        this.isLowland = isLowland;
    }

    public boolean getIsPeak() {
        return isPeak;
    }

    public void setIsPeak(boolean isPeak) {
        this.isPeak = isPeak;
    }
}
