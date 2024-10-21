package ru.postgrespro.perf.pgmicrobench.statanalyzer.DataLoader;

import java.util.Arrays;

public class Loader {
    private double[] latencies;

    public void loadLatencies(double[] latencies) {
        this.latencies = Arrays.copyOf(latencies, latencies.length);
    }

    public int getLatencyCount() {
        return latencies.length;
    }

    public double[] getLatencies() {
        return Arrays.copyOf(latencies, latencies.length);
    }
}
