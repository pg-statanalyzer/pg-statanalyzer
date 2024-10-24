package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.dataSets;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.UniformDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ModalityCloseModesDataSet {

    private static ModalityTestData generateSingle(Random random, double delta, int batch, String namePostfix) {
        String name = String.format("CloseModes(delta = %.2f, batch=%d)%s", delta, batch, namePostfix);

        List<Double> values = new ArrayList<>();
        values.addAll(UniformDistribution.generate(random, 0, 1, batch)
                .stream().map(x -> delta + Math.pow(x, 3)).collect(Collectors.toList()));
        values.addAll(UniformDistribution.generate(random, 0, 1, batch)
                .stream().map(x -> -delta - Math.pow(x, 3)).collect(Collectors.toList()));

        // Преобразуем список в массив double[]
        double[] valuesArray = values.stream().mapToDouble(Double::doubleValue).toArray();

        return new ModalityTestData(name, valuesArray, 2);
    }

    public static List<ModalityTestData> generate(Random random, String namePostfix) {
        List<ModalityTestData> dataSet = new ArrayList<>();

        int batchSize = 1000;
        dataSet.add(generateSingle(random, 0.5, batchSize, namePostfix));
        dataSet.add(generateSingle(random, 0.1, batchSize, namePostfix));
        dataSet.add(generateSingle(random, 0.01, batchSize, namePostfix));

        return dataSet;
    }
}
