package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Utility class that provides functionality to save benchmark results to JSON file
 */

public class JsonSaver {

    /**
     * Saves benchmark results to specified JSON file.
     * Benchmark results are retrieved from provided {@link BenchmarkResult} object
     * and written as list of blocks (each block being map of key-value pairs)
     *
     * @param result       {@link BenchmarkResult} object containing benchmark data to save
     * @param jsonFilePath file path to save resulting JSON file
     */
    public static void saveResultsToJson(BenchmarkResult result, String jsonFilePath) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            List<Map<String, Object>> dataBlocks = result.getAllBlocks();
            gson.toJson(dataBlocks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
