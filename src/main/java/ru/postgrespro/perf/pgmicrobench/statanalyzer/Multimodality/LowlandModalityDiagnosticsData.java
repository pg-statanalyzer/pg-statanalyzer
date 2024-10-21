package ru.postgrespro.perf.pgmicrobench.statanalyzer.Multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms.DensityHistogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms.DensityHistogramBin;

import java.text.NumberFormat;
import java.util.List;

public class LowlandModalityDiagnosticsData extends ModalityData {
    private final List<DiagnosticsBin> bins;

    public LowlandModalityDiagnosticsData(List<RangedMode> modes,
                                          DensityHistogram densityHistogram,
                                          List<DiagnosticsBin> bins) {
        super(modes, densityHistogram);
        this.bins = bins;
    }

    public List<DiagnosticsBin> getBins() {
        return bins;
    }

    public static class DiagnosticsBin {
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

    private String formatNumber(double value, NumberFormat numberFormat) {
        return numberFormat.format(value);
    }
}
