package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.SynchronizedHistogram;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Histograms {
    // All times should be stored in nanos.
    private static final long TIME_LOWEST_VALUE = 1_000L; // 1_000 ns is error rate, minimal trackable value
    private static final long TIME_HIGHEST_VALUE = 3_600_000_000_000L; // maximum value in the histogram (1 hour)
    private static final int SIGNIFICANT_DIGITS = 3; // maintain a value accuracy of ~0.1%

    /**
     * Histogram to track latency per iteration.
     */
    public static final Histogram iterationLatencyHistogram =
            new SynchronizedHistogram(TIME_LOWEST_VALUE, TIME_HIGHEST_VALUE, SIGNIFICANT_DIGITS);

    /**
     * Histogram to track transactions per second (TPS).
     */
    public static final Histogram tpsHistogram = new Histogram(1_000_000_000L, SIGNIFICANT_DIGITS);

    /**
     * A collection of all defined histograms, preserving insertion order using LinkedHashMap.
     */
    public static final HashMap<String, Histogram> histograms = new LinkedHashMap<>();

    static {
        histograms.put("latency", iterationLatencyHistogram);
        histograms.put("tps", tpsHistogram);
    }

    /**
     * Returns an histogram object by name. If the histogram doesn't exist, it creates
     * a new synchronized histogram with the default latency range and accuracy.
     *
     * @param histogramName the name of the histogram to return
     * @return the histogram corresponding to the given name
     */
    public static Histogram getUserHistogram(String histogramName) {
        if (!histograms.containsKey(histogramName)) {
            synchronized (histograms) {
                return histograms.computeIfAbsent(histogramName,
                        (h) -> new SynchronizedHistogram(TIME_LOWEST_VALUE, TIME_HIGHEST_VALUE, SIGNIFICANT_DIGITS));
            }
        }
        return histograms.get(histogramName);
    }
}
