package ru.postgrespro.perf.pgmicrobench.statanalyzer;


public class Analyzer {

    public static void main(String[] args) {

        String logFilePath = System.getProperty("logFilePath");

        if (logFilePath == null || logFilePath.isEmpty()) {
            System.err.println("Error: no file path provided. Please follow -DlogFilePath=\"/path/to/bench.log\"");
            return;
        }

        BenchmarkResult logResult = LogParser.parseLog(logFilePath);

        JsonSaver.saveResultsToJson(logResult, "log_results.json");

        // Sample
        System.out.println("Sample -- TPS overall: " + logResult.getParameter("tpsOverall"));
    }
}
