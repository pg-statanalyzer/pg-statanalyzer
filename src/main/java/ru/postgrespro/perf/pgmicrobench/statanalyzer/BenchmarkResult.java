package ru.postgrespro.perf.pgmicrobench.statanalyzer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Class that stores benchmark results, divided into multiple blocks of data.
 * Each block is represented as map of key-value pairs, where key is string
 * representing parameter name and value is object representing parameter's value
 */

public class BenchmarkResult {
    private List<Map<String, Object>> dataBlocks = new ArrayList<>();

    public void addBlock(Map<String, Object> block) {
        dataBlocks.add(block);
    }

    public List<Map<String, Object>> getAllBlocks() {
        return dataBlocks;
    }

    /**
     * Retrieves specific block of benchmark data by its index.
     *
     * @param index index of block to retrieve
     * @return block at specified index or null if index is out of bounds
     */
    public Map<String, Object> getBlock(int index) {
        if (index >= 0 && index < dataBlocks.size()) {
            return dataBlocks.get(index);
        }
        return null;
    }

    /**
     * Retrieves specific parameter from specified block of benchmark data.
     *
     * @param index index of block from which to retrieve parameter
     * @param key   key representing parameter to retrieve
     * @return value of parameter or null if block or parameter does not exist
     */
    public Object getParameterFromBlock(int index, String key) {
        Map<String, Object> block = getBlock(index);
        if (block != null) {
            return block.get(key);
        }
        return null;
    }
}
