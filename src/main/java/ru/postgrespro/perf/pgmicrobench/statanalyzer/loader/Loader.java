package ru.postgrespro.perf.pgmicrobench.statanalyzer.loader;

import java.util.Arrays;


/**
 * Class responsible for loading and managing array of latency values.
 * Provides methods to store, retrieve and query latency data.
 */

public class Loader {
    private double[] latencies;

    /**
     * Loads given array of latencies into this loader.
     * A copy of provided array is stored to avoid external modifications.
     *
     * @param latencies array of latency values to be loaded. Must not be {@code null}.
     */
    public void loadLatencies(double[] latencies) {
        this.latencies = Arrays.copyOf(latencies, latencies.length);
    }

    /**
     * Returns number of latencies stored in loader.
     *
     * @return count of latency values.
     */
    public int getLatencyCount() {
        return latencies.length;
    }

    /**
     * Returns copy of stored latencies.
     * This ensures that internal state remains immutable from outside changes.
     *
     * @return new array containing stored latencies.
     */
    public double[] getLatencies() {
        return Arrays.copyOf(latencies, latencies.length);
    }
}
