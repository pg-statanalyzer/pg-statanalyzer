package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import java.util.HashMap;
import java.util.Map;

public class BenchmarkResult {
    private Map<String, Object> parameters = new HashMap<>();

    public void addParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public Map<String, Object> getAllParameters() {
        return parameters;
    }
}
