package ru.postgrespro.perf.pgmicrobench.statanalyzer.loader;

import java.util.ArrayList;
import java.util.List;


/**
 * Class responsible for loading and managing array of latency values.
 * Provides methods to store, retrieve and query latency data.
 */

public class Loader {
    private List<Double> latencies;

    /**
     * Loads given list of latencies into this loader.
     * Copy of provided list is stored to avoid external modifications.
     *
     * @param latencies list of latency values to be loaded. Must not be {@code null}.
     */
    public void loadLatencies(List<Double> latencies) {
        this.latencies = new ArrayList<>(latencies);
    }

    /**
     * Returns number of latencies stored in loader.
     *
     * @return count of latency values.
     */
    public int getLatencyCount() {
        return latencies.size();
    }

    /**
     * Returns copy of stored latencies.
     * This ensures that internal state remains immutable from outside changes.
     *
     * @return new list containing stored latencies.
     */
    public List<Double> getLatencies() {
        return new ArrayList<>(latencies);
    }
}