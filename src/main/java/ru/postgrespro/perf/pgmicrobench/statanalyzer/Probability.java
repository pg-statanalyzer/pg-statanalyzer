package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Probability {
    public static List<Double> toProbabilities(double[] values) {
        return Arrays.stream(values)
                .boxed()
                .collect(Collectors.toList());
    }
}
