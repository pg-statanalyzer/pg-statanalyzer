package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import lombok.NonNull;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.IDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.QuantileRespectfulDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.WeightedSample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * LowlandModalityDetector class detects modality patterns from sample using density histogram.
 * <a href="https://aakinshin.net/posts/lowland-multimodality-detection/">A. Akinshin "Lowland Multimodality Detection"</a>
 */

public class LowlandModalityDetector {

    private final double sensitivity;
    private final double precision;
    private final boolean diagnostics;

	/**
	 * Constructs a LowlandModalityDetector with specified sensitivity and precision parameters.
	 * The detector uses a "lowland" algorithm which imagines that the histogram is a mountain relief (side view)
	 * and after "raining" it identifies modes. For more details, we recommend reading original article from
	 * Andrey Akinshin:
	 * <a href="https://aakinshin.net/posts/lowland-multimodality-detection/">A. Akinshin "Lowland Multimodality Detection"</a>
	 *
	 * @param sensitivity higher sensitivity allows you to detect small, almost hidden, modes,
	 *                    but increases the chance of false positive detections. On the other hand,
	 *                    with low sensitivity small or medium modes may be missed.
	 * @param precision histogram bin resolution where lower values
	 *                  provide finer resolution but may overfit to noise
	 * @param diagnostics enables collection of diagnostic information for analyzing density histograms
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
     * Detects modality data from given sample with default density histogram (QRDE-HD)
     *
     * @param sample input sample for modality detection.
     * @return ModalityData containing detected modes and related data.
     * @throws IllegalArgumentException if sample is null or contains less than two unique elements.
     */
    public ModalityData detectModes(WeightedSample sample) {
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
    public ModalityData detectModes(@NonNull WeightedSample sample,
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

	/**
	 * Finds local peaks in the density histogram bins
	 * A peak is a bin that is higher than its left neighbor and at least as high as its right neighbor.
	 *
	 * @param bins list of histogram bins
	 * @param binHeights list of bin heights
	 * @return list of indices representing peak positions in the histogram
	 */
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

	/**
	 * Analyzes detected peaks to find significant modes and cut points between them.
	 * Implements a peak merging algorithm where adjacent peaks are compared to determine
	 * if they represent distinct modes based on sensitivity.
	 *
	 * @param peaks list of peak indices from the histogram
	 * @param bins list of histogram bins
	 * @param binHeights list of bin heights
	 * @param histogram the density histogram
	 * @param diagnosticsBins array for diagnostic information if enabled
	 * @param sample original weighted sample
	 * @return {@link ModalityData} with detected modes and their characteristics
	 */
    private ModalityData analyzePeaks(List<Integer> peaks,
                                      List<DensityHistogramBin> bins,
                                      List<Double> binHeights,
                                      DensityHistogram histogram,
                                      DiagnosticsBin[] diagnosticsBins,
                                      WeightedSample sample) {

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

	/**
	 * Attempts to split the region between three peaks to identify distinct modes.
	 * Calculates the "lowland" area between peaks and compares it to sensitivity threshold
	 * to determine if peaks represent separate modes.
	 *
	 * @param peak0 first peak index in the candidate sequence
	 * @param peak1 middle peak index being evaluated
	 * @param peak2 third peak index in the candidate sequence
	 * @param bins list of histogram bins
	 * @param binHeights list of bin heights
	 * @param modeLocations list to add confirmed mode locations
	 * @param cutPoints list to add cut points between modes
	 * @param diagnosticsBins array for diagnostic markings
	 * @return true if successful split occurred and modes were added, false otherwise
	 */
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

	/**
	 * Creates the final {@link ModalityData} object from detected modes and cut points.
	 * Constructs RangedMode objects for each detected mode with their boundaries and sample data.
	 *
	 * @param modeLocations list of detected mode locations
	 * @param cutPoints list of cut points between modes
	 * @param bins list of histogram bins
	 * @param binHeights list of bin heights
	 * @param histogram the density histogram
	 * @param sample original weighted sample
	 * @param diagnosticsBins diagnostic information if enabled
	 * @return ModalityData containing all detected modes and histogram information
	 */
    private ModalityData createModalityData(List<Double> modeLocations,
                                            List<Double> cutPoints,
                                            List<DensityHistogramBin> bins,
                                            List<Double> binHeights,
                                            DensityHistogram histogram,
                                            WeightedSample sample,
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

	/**
	 * Creates a global mode when only one mode is detected in the entire distribution.
	 *
	 * @param bins list of histogram bins
	 * @param binHeights list of bin heights
	 * @param histogram the density histogram
	 * @param sample original sample data
	 * @return RangedMode representing the single global mode
	 */
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

	/**
	 * Creates a local mode within specified boundaries.
	 * Extracts sample data within the mode boundaries and creates a weighted sample
	 * representing the local mode distribution.
	 *
	 * @param modeLocation the central location of the mode
	 * @param lower lower boundary of the mode
	 * @param upper upper boundary of the mode
	 * @param sample original weighted sample
	 * @param bins list of histogram bins
	 * @return RangedMode representing the local mode with its boundaries and sample data
	 * @throws IllegalStateException if no values are found within the specified boundaries
	 */
    private RangedMode localMode(double modeLocation,
                                 double lower,
                                 double upper,
                                 WeightedSample sample,
                                 List<DensityHistogramBin> bins) {
        List<Double> modeValues = new ArrayList<>();
        List<Double> modeWeights = new ArrayList<>();

        for (DensityHistogramBin bin : bins) {
            double middle = bin.getMiddle();
            if (middle >= lower && middle <= upper) {
                modeValues.add(middle);
                modeWeights.add(sample.getWeightForBin(bin));

            }
        }

        if (modeValues.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Can't find any values in [%s, %s]", lower, upper));
        }

        Sample modeSample = new WeightedSample(modeValues, modeWeights);
        return new RangedMode(modeLocation, lower, upper, modeSample);
    }

	/**
	 * Finds the index of the minimum value in a sublist between given indices.
	 *
	 * @param values list of values to search
	 * @param start starting index (inclusive)
	 * @param end ending index (inclusive)
	 * @return index of the minimum value in the specified range
	 */
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

	/**
	 * Finds the index of the maximum value in a list.
	 *
	 * @param values list of values to search
	 * @return index of the maximum value in the list
	 */
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
