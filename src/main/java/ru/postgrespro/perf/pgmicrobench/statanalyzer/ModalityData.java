package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.util.List;

public class ModalityData {
    private final List<RangedMode> modes;
    private final DensityHistogram densityHistogram;

    public ModalityData(List<RangedMode> modes, DensityHistogram densityHistogram) {
        if (modes == null || densityHistogram == null) {
            throw new IllegalArgumentException("Modes and densityHistogram cannot be null");
        }
        this.modes = modes;
        this.densityHistogram = densityHistogram;
    }

    public List<RangedMode> getModes() {
        return modes;
    }

    public DensityHistogram getDensityHistogram() {
        return densityHistogram;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Detected Modes:\n");
        for (RangedMode mode : modes) {
            sb.append(mode).append("\n");
        }
        return sb.toString();
    }

    public int getModality() {
        return modes.size();
    }
}
