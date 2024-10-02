package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import java.util.HashMap;
import java.util.*;

public class BenchmarkResult {
    private List<Map<String, Object>> dataBlocks = new ArrayList<>();

    public void addBlock(Map<String, Object> block) {
        dataBlocks.add(block);
    }

    public List<Map<String, Object>> getAllBlocks() {
        return dataBlocks;
    }

    public Map<String, Object> getBlock(int index) {
        if (index >= 0 && index < dataBlocks.size()) {
            return dataBlocks.get(index);
        }
        return null;
    }

    public Object getParameterFromBlock(int index, String key) {
        Map<String, Object> block = getBlock(index);
        if (block != null) {
            return block.get(key);
        }
        return null;
    }
}
