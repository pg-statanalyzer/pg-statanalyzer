package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram;

import java.util.List;


/**
 * Represents modality data containing detected modes and their associated density histogram.
 * This class provides methods to access and analyze modes and histogram.
 */

public class ModalityData {

    private final List<RangedMode> modes;

    private final DensityHistogram densityHistogram;

    /**
     * Constructs {@code ModalityData} instance with specified modes and density histogram.
     *
     * @param modes            list of detected {@link RangedMode} objects.
     * @param densityHistogram {@link DensityHistogram} related to these modes.
     * @throws IllegalArgumentException if {@code modes} or {@code densityHistogram} are {@code null}.
     */
    public ModalityData(List<RangedMode> modes, DensityHistogram densityHistogram) {
        if (modes == null || densityHistogram == null) {
            throw new IllegalArgumentException("Modes and densityHistogram cannot be null");
        }
        this.modes = modes;
        this.densityHistogram = densityHistogram;
    }

    /**
     * Returns list of detected modes.
     *
     * @return unmodifiable list of {@link RangedMode} objects.
     */
    public List<RangedMode> getModes() {
        return modes;
    }

    /**
     * Returns density histogram associated with detected modes.
     *
     * @return {@link DensityHistogram} instance.
     */
    public DensityHistogram getDensityHistogram() {
        return densityHistogram;
    }

    /**
     * Returns string representation of modality data, including detected modes.
     *
     * @return string listing all detected modes.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Detected Modes:\n");
        for (RangedMode mode : modes) {
            sb.append(mode).append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns number of detected modes (modality).
     *
     * @return count of detected modes.
     */
    public int getModality() {
        return modes.size();
    }
}
