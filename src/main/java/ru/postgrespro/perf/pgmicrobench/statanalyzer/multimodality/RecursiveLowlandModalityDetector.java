package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import org.apache.commons.math3.util.Pair;
import org.knowm.xchart.Histogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * RecursiveLowlandModalityDetector is utility class for detecting and processing multimodal distributions
 * in histogram data using recursive approach. It includes methods for filtering bins that exceed given PDF
 * and ensuring smooth transitions across detected gaps.
 */

public class RecursiveLowlandModalityDetector {
    /**
     * Filters bins from histogram where estimated density is significantly higher than provided PDF.
     * This function smooths transitions across detected gaps to maintain data continuity
     *
     * @param sample input sample containing values to be processed
     * @param pdf    PDF used for comparison
     * @return filtered list of values that respect estimated PDF constraints
     */
    public static List<Double> filterBinsAbovePdf(Sample sample, Function<Double, Double> pdf) {

        int bins = (int) Math.sqrt(sample.size()) + 1;
        Histogram histogram = new Histogram(sample.getValues(), bins);

        List<Double> filteredData = new ArrayList<>();
        int totalDataCount = sample.size();

        List<Double> prevBinValues = null;
        List<Double> gapStart = null;

        List<Pair<List<Double>, List<Double>>> bridgePairs = new ArrayList<>();

        List<Double> values = sample.getValues();

        List<Double> xAxis = histogram.getxAxisData();
        List<Double> yAxis = histogram.getyAxisData();

        for (int i = 0; i < xAxis.size(); i++) {
            double binCenter = xAxis.get(i);
            double binHeight = yAxis.get(i);

            double binWidth = getBinWidth(xAxis, i);
            double normalizedHeight = binHeight / (totalDataCount * binWidth);
            double pdfValue = pdf.apply(binCenter);

            double leftBound = xAxis.get(i);
            double rightBound = (i + 1 < xAxis.size()) ? xAxis.get(i + 1) : leftBound + binWidth;

            List<Double> binValues = getBinValuesInRange(values, leftBound, rightBound);

            if (normalizedHeight >= pdfValue) {
                double ratio = pdfValue / normalizedHeight;

                if (ratio < 0.45) {
                    if (gapStart != null && prevBinValues != null && !binValues.isEmpty()) {
                        bridgePairs.add(new Pair<>(prevBinValues, binValues));
                    }
                    filteredData.addAll(binValues);
                    gapStart = null;
                    prevBinValues = binValues;
                } else if (gapStart == null && !binValues.isEmpty()) {
                    gapStart = binValues;
                }
            } else if (gapStart == null && !binValues.isEmpty()) {
                gapStart = binValues;
            }
        }

        for (int i = 0; i < bridgePairs.size() - 1; i++) {
            List<Double> left = bridgePairs.get(i).getKey();
            List<Double> right = bridgePairs.get(i).getValue();

            double leftTail = getTailValue(left, true);
            double rightTail = getTailValue(right, false);

            double prevStd = estimateStdDev(left);
            double nextStd = estimateStdDev(right);
            double avgStd = prevStd + nextStd + 1e-6;
            double distance = Math.abs(rightTail - leftTail);

            int steps = (int) Math.max(50, Math.min(500, distance / avgStd * 40));

            filteredData.addAll(generateSmoothTransition(leftTail, rightTail, prevStd, nextStd, steps));
        }

        return filteredData;
    }

    /**
     * Calculates width of histogram bin based on its index and x-axis bin centers.
     *
     * @param xAxis list of bin center values
     * @param i     index of current bin
     * @return estimated width of bin at index {@code i}
     */
    private static double getBinWidth(List<Double> xAxis, int i) {
        if (i + 1 < xAxis.size()) {
            return xAxis.get(i + 1) - xAxis.get(i);
        } else if (i > 0) {
            return xAxis.get(i) - xAxis.get(i - 1);
        } else {
            return 1.0;
        }
    }

    /**
     * Filters values within specified range [left, right).
     *
     * @param values list of values to filter
     * @param left   inclusive lower bound of range
     * @param right  exclusive upper bound of range
     * @return list of values in range [left, right)
     */
    private static List<Double> getBinValuesInRange(List<Double> values, double left, double right) {
        return values.stream()
                .filter(v -> v >= left && v < right)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves tail value of list, either from beginning or end.
     * Returns the first element if {@code fromEnd} is {@code false}, or the last element
     * if {@code fromEnd} is {@code true}
     *
     * @param values  list of values
     * @param fromEnd {@code true} to get the last value, {@code false} for the first
     * @return selected edge value, or 0.0 if list is null or empty
     */
    private static double getTailValue(List<Double> values, boolean fromEnd) {
        if (values == null || values.isEmpty()) return 0.0;
        return fromEnd ? values.get(values.size() - 1) : values.get(0);
    }

    /**
     * Estimates standard deviation of list of numeric values.
     * Uses standard deviation formula:
     * sqrt(mean((x - mean)^2))
     *
     * @param values list of numeric values
     * @return estimated standard deviation, or 1.0 if input is invalid
     */
    private static double estimateStdDev(List<Double> values) {
        if (values == null || values.size() < 2) return 1.0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * Generates smooth transition between two values using Hermite interpolation (smooth S-curve)
     * and slight parabolic sag in the middle.
     * This method is used to bridge gaps between modes in distribution, simulating
     * smooth structure. Interpolation adds slight dip in the center of transition
     * based on average of two standard deviations
     *
     * @param start   starting value of transition
     * @param end     ending value of transition
     * @param prevStd estimated standard deviation of the previous mode
     * @param nextStd estimated standard deviation of the next mode
     * @param steps   number of interpolation steps to generate
     * @return list of interpolated values forming smooth transition from start to end
     */
    private static List<Double> generateSmoothTransition(
            double start, double end, double prevStd, double nextStd, int steps) {

        List<Double> transition = new ArrayList<>();
        double range = end - start;
        double avgStd = (prevStd + nextStd + 1e-6);
        double sagFactor = 0.15 * avgStd;

        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1);
            double smoothT = t * t * (3 - 2 * t);
            double interpolated = start + smoothT * range;
            interpolated += -sagFactor * Math.pow(t - 0.5, 2);
            transition.add(interpolated);
        }

        return transition;
    }
}
