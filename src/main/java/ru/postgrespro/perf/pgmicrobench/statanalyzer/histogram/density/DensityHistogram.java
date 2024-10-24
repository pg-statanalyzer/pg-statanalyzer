package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * Represents density histogram with list of bins, where each bin covers range of values and has corresponding height.
 * Histogram is immutable once created.
 */

public final class DensityHistogram {
    private final List<DensityHistogramBin> bins;

    /**
     * Constructs {@code DensityHistogram} with specified bins.
     *
     * @param bins list of {@link DensityHistogramBin} objects representing histogram's bins.
     *             Must not be {@code null} or empty.
     * @throws IllegalArgumentException if list of bins is {@code null} or empty.
     */
    public DensityHistogram(List<DensityHistogramBin> bins) {
        if (bins == null || bins.isEmpty()) {
            throw new IllegalArgumentException("Bins cannot be null or empty");
        }
        this.bins = Collections.unmodifiableList(bins);
    }

    /**
     * Gets lower bound of the first bin in histogram.
     *
     * @return lower bound of the first bin.
     */
    public double getGlobalLower() {
        return bins.get(0).lower();
    }

    /**
     * Gets upper bound of the last bin in histogram.
     *
     * @return upper bound of the last bin.
     */
    public double getGlobalUpper() {
        return bins.get(bins.size() - 1).upper();
    }

    /**
     * Returns string representation of histogram in specified format and locale.
     * Each bin is presented in format: {@code [lower; upper]: height}.
     *
     * @param format format string for displaying numbers (e.g., {@code "%.2f"}).
     * @param locale {@link Locale} to apply when formatting numbers.
     * @return formatted string representing histogram.
     */
    public String present(String format, Locale locale) {
        StringBuilder builder = new StringBuilder();
        for (DensityHistogramBin bin : bins) {
            builder.append(String.format(locale, "[%s; %s]: %s%n",
                    String.format(locale, format, bin.lower()),
                    String.format(locale, format, bin.upper()),
                    String.format(locale, format, bin.height())));
        }
        return builder.toString().trim();
    }

    /**
     * Returns list of bins in histogram. Returned list is unmodifiable.
     *
     * @return unmodifiable list of {@link DensityHistogramBin} objects.
     */
    public List<DensityHistogramBin> bins() {
        return bins;
    }

    /**
     * Returns hash code of this histogram based on its bins.
     *
     * @return hash code of this histogram.
     */
    @Override
    public int hashCode() {
        return Objects.hash(bins);
    }

    /**
     * Returns string representation of histogram, including list of bins.
     *
     * @return string representation of this {@code DensityHistogram}.
     */
    @Override
    public String toString() {
        return "f.Histograms.DensityHistogram[" +
                "bins=" + bins + ']';
    }
}
