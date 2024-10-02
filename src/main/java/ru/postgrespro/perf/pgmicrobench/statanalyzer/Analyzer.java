package ru.postgrespro.perf.pgmicrobench.statanalyzer;


public class Analyzer {

    public static void main(String[] args) {

        String logFilePath = System.getProperty("logFilePath");

        if (logFilePath == null || logFilePath.isEmpty()) {
            System.err.println("Error: no file path provided. Please follow -DlogFilePath=\"/path/to/bench.log\"");
            return;
        }

        BenchmarkResult logResult = LogParser.parseLog(logFilePath);

        JsonSaver.saveResultsToJson(logResult, "bench_results.json");

        // Sample
        for (int i = 0; i < logResult.getAllBlocks().size(); i++) {
            System.out.println("Iterations: " + logResult.getParameterFromBlock(i, "iterations"));
            System.out.println("TPS overall: " + logResult.getParameterFromBlock(i, "tpsOverall"));
            System.out.println("Latency: " + logResult.getParameterFromBlock(i, "latency"));
            System.out.println();
        }
    }
}
