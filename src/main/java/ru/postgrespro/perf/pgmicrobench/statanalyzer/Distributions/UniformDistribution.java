package ru.postgrespro.perf.pgmicrobench.statanalyzer.Distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UniformDistribution {

    public static List<Double> generate(Random random, double min, double max, int count) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double value = min + (max - min) * random.nextDouble();
            values.add(value);
        }
        return values;
    }
}
