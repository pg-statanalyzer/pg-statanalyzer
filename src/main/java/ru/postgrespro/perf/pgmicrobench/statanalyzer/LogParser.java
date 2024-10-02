package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    private static final Pattern MSEC_PATTERN = Pattern.compile("Test completed after (\\d+) msec");
    private static final Pattern ITERATIONS_PATTERN = Pattern.compile("- (\\d+) iterations");
    private static final Pattern TPS_OVERALL_PATTERN = Pattern.compile("- (\\d+) tps \\(overall\\)");
    private static final Pattern TPS_LAST_5_SEC_PATTERN = Pattern.compile("- (\\d+) tps \\(last 5 sec\\)");
    private static final Pattern LATENCY_PATTERN = Pattern.compile("- (\\d+) ns average latency");

    public static BenchmarkResult parseLog(String logFilePath) {
        BenchmarkResult result = new BenchmarkResult();
        Map<String, Object> currentBlock = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Matcher msecMatcher = MSEC_PATTERN.matcher(line);

                if (msecMatcher.find()) {
                    if (currentBlock != null) {
                        result.addBlock(currentBlock);
                    }
                    currentBlock = new HashMap<>();
                    currentBlock.put("msec", Integer.parseInt(msecMatcher.group(1)));
                }

                if (currentBlock != null) {
                    Matcher iterationsMatcher = ITERATIONS_PATTERN.matcher(line);
                    Matcher tpsOverallMatcher = TPS_OVERALL_PATTERN.matcher(line);
                    Matcher tpsLast5SecMatcher = TPS_LAST_5_SEC_PATTERN.matcher(line);
                    Matcher latencyMatcher = LATENCY_PATTERN.matcher(line);

                    if (iterationsMatcher.find()) {
                        currentBlock.put("iterations", Integer.parseInt(iterationsMatcher.group(1)));
                    }
                    if (tpsOverallMatcher.find()) {
                        currentBlock.put("tpsOverall", Integer.parseInt(tpsOverallMatcher.group(1)));
                    }
                    if (tpsLast5SecMatcher.find()) {
                        currentBlock.put("tpsLast5Sec", Integer.parseInt(tpsLast5SecMatcher.group(1)));
                    }
                    if (latencyMatcher.find()) {
                        currentBlock.put("latency", Long.parseLong(latencyMatcher.group(1)));
                    }
                }
            }

            if (currentBlock != null) {
                result.addBlock(currentBlock);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
