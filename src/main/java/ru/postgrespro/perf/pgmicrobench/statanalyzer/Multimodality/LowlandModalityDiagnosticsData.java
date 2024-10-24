package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram;

import java.text.NumberFormat;
import java.util.List;


/**
 * Represents diagnostic data for lowland modalities, extending {@link ModalityData} class.
 * This class provides additional information with list of {@link DiagnosticsBin} objects.
 */

public class LowlandModalityDiagnosticsData extends ModalityData {

    /**
     * Constructs {@code LowlandModalityDiagnosticsData} instance with specified modes,
     * density histogram and diagnostic bins.
     *
     * @param modes            list of {@link RangedMode} representing detected modes.
     * @param densityHistogram {@link DensityHistogram} associated with this data.
     * @param bins             list of {@link DiagnosticsBin} providing diagnostic information.
     */
    public LowlandModalityDiagnosticsData(List<RangedMode> modes,
                                          DensityHistogram densityHistogram,
                                          List<DiagnosticsBin> bins) {
        super(modes, densityHistogram);
    }

    /**
     * Formats number using specified {@link NumberFormat}.
     *
     * @param value        numeric value to format.
     * @param numberFormat {@link NumberFormat} to apply.
     * @return formatted string representation of value.
     */
    private String formatNumber(double value, NumberFormat numberFormat) {
        return numberFormat.format(value);
    }
}
