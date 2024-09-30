package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class JsonSaver {

    public static void saveResultsToJson(BenchmarkResult result, String jsonFilePath) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            Map<String, Object> parameters = result.getAllParameters();
            gson.toJson(parameters, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
