package ru.postgrespro.perf.pgmicrobench.statanalyzer.Histograms;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class DensityHistogram {
    private final List<DensityHistogramBin> bins;

    public DensityHistogram(List<DensityHistogramBin> bins) {
        if (bins == null || bins.isEmpty()) {
            throw new IllegalArgumentException("Bins cannot be null or empty");
        }
        this.bins = Collections.unmodifiableList(bins);
    }

    public double getGlobalLower() {
        return bins.get(0).lower();
    }

    public double getGlobalUpper() {
        return bins.get(bins.size() - 1).upper();
    }

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

    public List<DensityHistogramBin> bins() {
        return bins;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bins);
    }

    @Override
    public String toString() {
        return "f.Histograms.DensityHistogram[" +
                "bins=" + bins + ']';
    }
}
