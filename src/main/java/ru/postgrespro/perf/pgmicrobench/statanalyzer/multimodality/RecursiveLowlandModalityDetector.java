package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import org.knowm.xchart.Histogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


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

        int totalDataCount = sample.getValues().size();

        List<Double> lastBinValues = new ArrayList<>();
        boolean inGap = false;
        double lastValueBeforeGap = Double.NaN;
        double nextValueAfterGap;

        for (int i = 0; i < histogram.getxAxisData().size(); i++) {
            double binCenter = histogram.getxAxisData().get(i);
            double binHeight = histogram.getyAxisData().get(i);
            double binWidth = (i + 1 < histogram.getxAxisData().size()) ?
                    histogram.getxAxisData().get(i + 1) - binCenter : 0;

            double normalizedBinHeight = binHeight / (totalDataCount * binWidth);
            double pdfValue = pdf.apply(binCenter);

            double leftBound = histogram.getxAxisData().get(i);
            double rightBound = (i + 1 < histogram.getxAxisData().size())
                    ? histogram.getxAxisData().get(i + 1)
                    : leftBound + (leftBound - histogram.getxAxisData().get(i - 1));

            List<Double> binValues = new ArrayList<>();
            for (Double value : sample.getValues()) {
                if (value >= leftBound && value < rightBound) {
                    binValues.add(value);
                }
            }

            if (normalizedBinHeight >= pdfValue) {
                double ratio = pdfValue / normalizedBinHeight;

                if (ratio < 0.45) {
                    filteredData.addAll(binValues);
                    lastBinValues = new ArrayList<>(binValues);
                    inGap = false;
                } else {
                    if (!inGap) {
                        inGap = true;
                        lastValueBeforeGap = lastBinValues.isEmpty() ? Double.NaN : lastBinValues.get(lastBinValues.size() - 1);
                    }
                }
            } else {
                if (inGap) {
                    nextValueAfterGap = binValues.isEmpty() ? Double.NaN : binValues.get(0);
                    if (!Double.isNaN(lastValueBeforeGap) && !Double.isNaN(nextValueAfterGap)) {
                        filteredData.addAll(generateSmoothTransition(lastValueBeforeGap, nextValueAfterGap, 100));
                    }
                    inGap = false;
                }
            }
        }
        return filteredData;
    }

    /**
     * Generates smooth transition between two values using linear interpolation with slight sinusoidal adjustment.
     * This helps in filling gaps in data while maintaining natural-looking distribution
     *
     * @param start starting value of transition
     * @param end   ending value of transition
     * @param steps number of interpolation steps
     * @return list of interpolated values forming smooth transition
     */
    private static List<Double> generateSmoothTransition(double start, double end, int steps) {
        List<Double> transition = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1);
            double value = start * (1 - t) + end * t;
            value += Math.sin(t * Math.PI) * (end - start) * 0.1;
            transition.add(value);
        }
        return transition;
    }
}
