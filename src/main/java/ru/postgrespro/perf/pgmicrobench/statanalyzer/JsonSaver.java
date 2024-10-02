package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JsonSaver {

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
