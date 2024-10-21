package ru.postgrespro.perf.pgmicrobench.statanalyzer.Multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms.DensityHistogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms.DensityHistogramBin;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms.IDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms.QuantileRespectfulDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.*;
public class LowlandModalityDetector {

    private final double sensitivity;
    private final double precision;
    private final boolean diagnostics;

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

    public LowlandModalityDetector() {
        this(0.5, 0.01, false);
    }

    public ModalityData detectModes(Sample sample) {
        return detectModes(sample, QuantileRespectfulDensityHistogramBuilder.getInstance());
    }

    public ModalityData detectModes(Sample sample, IDensityHistogramBuilder densityHistogramBuilder) {
        if (sample == null) {
            throw new IllegalArgumentException("f.Sample cannot be null");
        }
        if (sample.getMax() - sample.getMin() < 1e-9) {
            throw new IllegalArgumentException("f.Sample should contain at least two different elements");
        }

        densityHistogramBuilder = Optional.ofNullable(densityHistogramBuilder)
                .orElse(QuantileRespectfulDensityHistogramBuilder.getInstance());

        int desiredBinCount = (int) Math.round(1 / precision);
        DensityHistogram histogram = densityHistogramBuilder.build(sample, desiredBinCount);
        List<DensityHistogramBin> bins = histogram.bins();
        List<Double> binHeights = new ArrayList<>();

        for (DensityHistogramBin bin : bins) {
            binHeights.add(bin.height());
        }

        List<Integer> peaks = new ArrayList<>();
        LowlandModalityDiagnosticsData.DiagnosticsBin[] diagnosticsBins = diagnostics
                ? bins.stream().map(LowlandModalityDiagnosticsData.DiagnosticsBin::new).toArray(LowlandModalityDiagnosticsData.DiagnosticsBin[]::new)
                : new LowlandModalityDiagnosticsData.DiagnosticsBin[0];

        for (int i = 1; i < bins.size() - 1; i++) {
            if (binHeights.get(i) > binHeights.get(i - 1) && binHeights.get(i) >= binHeights.get(i + 1)) {
                peaks.add(i);
                if (diagnostics) {
                    diagnosticsBins[i].setIsPeak(true);
                }
            }
        }

        return analyzePeaks(peaks, bins, binHeights, histogram, diagnosticsBins, sample);
    }

    private ModalityData analyzePeaks(List<Integer> peaks, List<DensityHistogramBin> bins, List<Double> binHeights,
                                      DensityHistogram histogram, LowlandModalityDiagnosticsData.DiagnosticsBin[] diagnosticsBins, Sample sample) {

        List<Double> modeLocations = new ArrayList<>();
        List<Double> cutPoints = new ArrayList<>();

        List<Integer> previousPeaks = new ArrayList<>();
        if (!peaks.isEmpty()) previousPeaks.add(peaks.get(0));

        for (int i = 1; i < peaks.size(); i++) {
            int currentPeak = peaks.get(i);

            while (!previousPeaks.isEmpty() && binHeights.get(previousPeaks.get(previousPeaks.size() - 1)) < binHeights.get(currentPeak)) {
                if (trySplit(previousPeaks.get(0), previousPeaks.get(previousPeaks.size() - 1), currentPeak, bins, binHeights, modeLocations, cutPoints, diagnosticsBins)) {
                    previousPeaks.clear();
                } else {
                    previousPeaks.remove(previousPeaks.size() - 1);
                }
            }

            if (!previousPeaks.isEmpty() && binHeights.get(previousPeaks.get(previousPeaks.size() - 1)) > binHeights.get(currentPeak)) {
                if (trySplit(previousPeaks.get(0), previousPeaks.get(previousPeaks.size() - 1), currentPeak, bins, binHeights, modeLocations, cutPoints, diagnosticsBins)) {
                    previousPeaks.clear();
                }
            }

            previousPeaks.add(currentPeak);
        }

        if (!previousPeaks.isEmpty()) {
            modeLocations.add(bins.get(previousPeaks.get(0)).getMiddle());
        }

        return createModalityData(modeLocations, cutPoints, bins, binHeights, histogram, sample, diagnosticsBins);
    }

    private boolean trySplit(int peak0, int peak1, int peak2, List<DensityHistogramBin> bins, List<Double> binHeights,
                             List<Double> modeLocations, List<Double> cutPoints, LowlandModalityDiagnosticsData.DiagnosticsBin[] diagnosticsBins) {
        int left = peak1, right = peak2;
        double height = Math.min(binHeights.get(peak1), binHeights.get(peak2));

        while (left < right && binHeights.get(left) > height) left++;
        while (left < right && binHeights.get(right) > height) right--;

        if (diagnostics) {
            for (int i = left; i <= right; i++) {
                diagnosticsBins[i].setWaterLevel(height);
            }
        }

        double width = bins.get(right).upper() - bins.get(left).lower();
        double totalArea = width * height;
        double binArea = 1.0 / bins.size();
        double totalBinArea = (right - left + 1) * binArea;
        double binProportion = totalBinArea / totalArea;

        if (binProportion < sensitivity) {
            modeLocations.add(bins.get(peak0).getMiddle());
            cutPoints.add(bins.get(whichMin(binHeights, peak1, peak2)).getMiddle());

            if (diagnostics) {
                diagnosticsBins[peak0].setIsMode(true);
                for (int i = left; i <= right; i++) {
                    diagnosticsBins[i].setIsLowland(true);
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

    private ModalityData createModalityData(List<Double> modeLocations, List<Double> cutPoints,
                                            List<DensityHistogramBin> bins, List<Double> binHeights,
                                            DensityHistogram histogram, Sample sample,
                                            LowlandModalityDiagnosticsData.DiagnosticsBin[] diagnosticsBins) {

        List<RangedMode> modes = new ArrayList<>();
        if (modeLocations.size() <= 1) {
            modes.add(globalMode(bins, binHeights, histogram, sample));
        } else {
            modes.add(localMode(modeLocations.get(0), histogram.getGlobalLower(), cutPoints.get(0), sample, bins));
            for (int i = 1; i < modeLocations.size() - 1; i++) {
                modes.add(localMode(modeLocations.get(i), cutPoints.get(i - 1), cutPoints.get(i), sample, bins));
            }
            modes.add(localMode(modeLocations.get(modeLocations.size() - 1), cutPoints.get(cutPoints.size() - 1), histogram.getGlobalUpper(), sample, bins));
        }

        return diagnostics
                ? new LowlandModalityDiagnosticsData(modes, histogram, Arrays.asList(diagnosticsBins))
                : new ModalityData(modes, histogram);
    }

    private RangedMode globalMode(List<DensityHistogramBin> bins, List<Double> binHeights, DensityHistogram histogram, Sample sample) {
        int maxIndex = whichMax(binHeights);
        return new RangedMode(bins.get(maxIndex).getMiddle(), histogram.getGlobalLower(), histogram.getGlobalUpper(), sample);
    }

    private RangedMode localMode(double modeLocation, double lower, double upper, Sample sample, List<DensityHistogramBin> bins) {
        List<Double> modeValues = new ArrayList<>();
        List<Double> modeWeights = sample.isWeighted ? new ArrayList<>() : null;

        for (DensityHistogramBin bin : bins) {
            double middle = bin.getMiddle();
            if (middle >= lower && middle <= upper) {
                modeValues.add(middle); // Add the value of the bin's middle if within bounds
                if (modeWeights != null) {
                    modeWeights.add(sample.getWeightForBin(bin)); // Assuming a method exists to get weight for the bin
                }
            }
        }

        if (modeValues.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Can't find any values in [%s, %s]", lower, upper));
        }

        Sample modeSample = modeWeights == null ? new Sample(modeValues) : new Sample(modeValues, modeWeights);
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
