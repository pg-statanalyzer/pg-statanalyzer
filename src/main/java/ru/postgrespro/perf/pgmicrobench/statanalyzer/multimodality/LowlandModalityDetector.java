package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import lombok.NonNull;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.IDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.QuantileRespectfulDensityHistogramBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * LowlandModalityDetector class detects modality patterns from sample using density histogram.
 */

public class LowlandModalityDetector {

    private final double sensitivity;
    private final double precision;
    private final boolean diagnostics;

    /**
     * Detects modality patterns from sample using density histogram.
     */
    public LowlandModalityDetector(double sensitivity, double precision, boolean diagnostics) {
        if (sensitivity < 0 || sensitivity > 1) {
            throw new IllegalArgumentException("Sensitivity must be between 0 and 1.");
        }
        if (precision <= 0 || precision >= 1) {
            throw new IllegalArgumentException("Precision must be between 0 and 1.");
        }
        this.sensitivity = sensitivity;
        this.precision = precision;
        this.diagnostics = diagnostics;
    }

    /**
     * Detects modality data from given sample.
     *
     * @param sample input sample for modality detection.
     * @return ModalityData containing detected modes and related data.
     * @throws IllegalArgumentException if sample is null or contains less than two unique elements.
     */
    public ModalityData detectModes(Sample sample) {
        return detectModes(sample, QuantileRespectfulDensityHistogramBuilder.getInstance());
    }

    /**
     * Detects modality data from given sample using specified density histogram builder.
     *
     * @param sample                  input sample for modality detection.
     * @param densityHistogramBuilder builder for constructing density histogram.
     * @return ModalityData containing detected modes and related data.
     * @throws IllegalArgumentException if sample is null or contains less than two unique elements.
     */
    public ModalityData detectModes(@NonNull Sample sample,
                                    IDensityHistogramBuilder densityHistogramBuilder) {
        if (sample.getMax() - sample.getMin() < 1e-9) {
            throw new IllegalArgumentException("Sample should contain at least two different elements");
        }

        densityHistogramBuilder = Optional.ofNullable(densityHistogramBuilder)
                .orElse(QuantileRespectfulDensityHistogramBuilder.getInstance());

        int desiredBinCount = (int) Math.round(1 / precision);
        DensityHistogram histogram = densityHistogramBuilder.build(sample, desiredBinCount);
        List<DensityHistogramBin> bins = histogram.getBins();
        List<Double> binHeights = new ArrayList<>();

        for (DensityHistogramBin bin : bins) {
            binHeights.add(bin.getHeight());
        }

        List<Integer> peaks = findPeaks(bins, binHeights);

        DiagnosticsBin[] diagnosticsBins = diagnostics
                ? bins.stream().map(DiagnosticsBin::new).toArray(DiagnosticsBin[]::new)
                : new DiagnosticsBin[0];

        return analyzePeaks(peaks, bins, binHeights, histogram, diagnosticsBins, sample);
    }

    private List<Integer> findPeaks(List<DensityHistogramBin> bins, List<Double> binHeights) {
        List<Integer> peaks = new ArrayList<>();
        for (int i = 1; i < bins.size() - 1; i++) {
            if (binHeights.get(i) > binHeights.get(i - 1)
                    && binHeights.get(i) >= binHeights.get(i + 1)) {
                peaks.add(i);
            }
        }
        return peaks;
    }

    private ModalityData analyzePeaks(List<Integer> peaks,
                                      List<DensityHistogramBin> bins,
                                      List<Double> binHeights,
                                      DensityHistogram histogram,
                                      DiagnosticsBin[] diagnosticsBins,
                                      Sample sample) {

        List<Double> modeLocations = new ArrayList<>();
        List<Double> cutPoints = new ArrayList<>();

        List<Integer> previousPeaks = new ArrayList<>();

        if (!peaks.isEmpty()) {
            previousPeaks.add(peaks.get(0));
        }

        for (int i = 1; i < peaks.size(); i++) {
            int currentPeak = peaks.get(i);

            while (!previousPeaks.isEmpty() && binHeights
                    .get(previousPeaks.get(previousPeaks.size() - 1))
                    < binHeights.get(currentPeak)) {

                if (trySplit(previousPeaks.get(0),
                        previousPeaks.get(previousPeaks.size() - 1),
                        currentPeak,
                        bins,
                        binHeights,
                        modeLocations,
                        cutPoints,
                        diagnosticsBins)) {
                    previousPeaks.clear();
                } else {
                    previousPeaks.remove(previousPeaks.size() - 1);
                }
            }

            if (!previousPeaks.isEmpty() && binHeights
                    .get(previousPeaks.get(previousPeaks.size() - 1))
                    > binHeights.get(currentPeak)) {

                if (trySplit(previousPeaks.get(0),
                        previousPeaks.get(previousPeaks.size() - 1),
                        currentPeak,
                        bins,
                        binHeights,
                        modeLocations,
                        cutPoints,
                        diagnosticsBins)) {
                    previousPeaks.clear();
                }
            }

            previousPeaks.add(currentPeak);
        }

        if (!previousPeaks.isEmpty()) {
            modeLocations.add(bins.get(previousPeaks.get(0)).getMiddle());
        }

        return createModalityData(modeLocations,
                cutPoints,
                bins,
                binHeights,
                histogram,
                sample,
                diagnosticsBins);
    }

    private boolean trySplit(int peak0,
                             int peak1,
                             int peak2,
                             List<DensityHistogramBin> bins,
                             List<Double> binHeights,
                             List<Double> modeLocations,
                             List<Double> cutPoints,
                             DiagnosticsBin[] diagnosticsBins) {

        int left = peak1;
        int right = peak2;
        double height = Math.min(binHeights.get(peak1), binHeights.get(peak2));

        while (left < right && binHeights.get(left) > height) {
            left++;
        }
        while (left < right && binHeights.get(right) > height) {
            right--;
        }

        if (diagnostics) {
            for (int i = left; i <= right; i++) {
                diagnosticsBins[i].setWaterLevel(height);
            }
        }

        double width = bins.get(right).getUpper() - bins.get(left).getLower();
        double totalArea = width * height;
        double binArea = 1.0 / bins.size();
        double totalBinArea = (right - left + 1) * binArea;
        double binProportion = totalBinArea / totalArea;

        if (binProportion < sensitivity) {

            modeLocations.add(bins.get(peak0).getMiddle());
            cutPoints.add(bins.get(whichMin(binHeights, peak1, peak2)).getMiddle());

            if (diagnostics) {
                diagnosticsBins[peak0].setMode(true);
                for (int i = left; i <= right; i++) {
                    diagnosticsBins[i].setLowland(true);
                }
            }
            return true;
        }
        return false;
    }

    private int whichMin(List<Double> values, int start, int end) {
        double min = values.get(start);
        int minIndex = start;
        for (int i = start + 1; i <= end; i++) {
            if (values.get(i) < min) {
                min = values.get(i);
                minIndex = i;
            }
        }
        return minIndex;
    }

    private ModalityData createModalityData(List<Double> modeLocations,
                                            List<Double> cutPoints,
                                            List<DensityHistogramBin> bins,
                                            List<Double> binHeights,
                                            DensityHistogram histogram,
                                            Sample sample,
                                            DiagnosticsBin[] diagnosticsBins) {

        List<RangedMode> modes = new ArrayList<>();
        if (modeLocations.size() <= 1) {
            modes.add(globalMode(bins, binHeights, histogram, sample));
        } else {
            modes.add(localMode(modeLocations.get(0),
                    histogram.getGlobalLower(),
                    cutPoints.get(0),
                    sample,
                    bins));

            for (int i = 1; i < modeLocations.size() - 1; i++) {
                modes.add(localMode(modeLocations.get(i),
                        cutPoints.get(i - 1),
                        cutPoints.get(i),
                        sample,
                        bins));
            }

            modes.add(localMode(modeLocations.get(modeLocations.size() - 1),
                    cutPoints.get(cutPoints.size() - 1),
                    histogram.getGlobalUpper(),
                    sample,
                    bins));
        }

        return diagnostics
                ? new LowlandModalityDiagnosticsData(modes,
                histogram,
                Arrays.asList(diagnosticsBins))
                : new ModalityData(modes, histogram);
    }

    private RangedMode globalMode(List<DensityHistogramBin> bins,
                                  List<Double> binHeights,
                                  DensityHistogram histogram,
                                  Sample sample) {
        int maxIndex = whichMax(binHeights);
        return new RangedMode(bins.get(maxIndex).getMiddle(),
                histogram.getGlobalLower(),
                histogram.getGlobalUpper(),
                sample);
    }

    private RangedMode localMode(double modeLocation,
                                 double lower,
                                 double upper,
                                 Sample sample,
                                 List<DensityHistogramBin> bins) {
        List<Double> modeValues = new ArrayList<>();
        List<Double> modeWeights = sample.isWeighted() ? new ArrayList<>() : null;

        for (DensityHistogramBin bin : bins) {
            double middle = bin.getMiddle();
            if (middle >= lower && middle <= upper) {
                modeValues.add(middle);
                if (modeWeights != null) {
                    modeWeights.add(sample.getWeightForBin(bin));
                }
            }
        }

        if (modeValues.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Can't find any values in [%s, %s]", lower, upper));
        }

        Sample modeSample = modeWeights == null ? new Sample(modeValues, true) : new Sample(modeValues, modeWeights);
        return new RangedMode(modeLocation, lower, upper, modeSample);
    }

    private int whichMax(List<Double> values) {
        double max = values.get(0);
        int maxIndex = 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > max) {
                max = values.get(i);
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
