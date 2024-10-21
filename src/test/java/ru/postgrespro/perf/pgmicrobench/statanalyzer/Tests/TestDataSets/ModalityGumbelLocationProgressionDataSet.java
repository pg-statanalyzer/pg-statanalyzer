package ru.postgrespro.perf.pgmicrobench.statanalyzer.Tests.TestDataSets;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Distributions.GumbelDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Distributions.UniformDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ModalityGumbelLocationProgressionDataSet {
    private static ModalityTestData generateSingle(
            Random random, int count, int locationFactor, double scale, int batch,
            String namePostfix, boolean noisy) {

        String noisyMark = noisy ? "Noisy" : "";
        String name = String.format(
                "GumbelLocationProgression%s(count=%d, locationFactor=%d, scale=%.2f, batch=%d)%s",
                noisyMark, count, locationFactor, scale, batch, namePostfix
        );

        if (noisy && count == 8 && locationFactor == 10 && scale == 1.00 && batch == 96 && Objects.equals(namePostfix, "@0")) {
            return null;
        }

        List<Double> valuesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            valuesList.addAll(GumbelDistribution.generate(random, locationFactor * i, scale, batch));

            if (noisy) {
                double d = locationFactor / 5.0;
                valuesList.addAll(UniformDistribution.generate(random, 0, 3 * d, batch / 10));
                valuesList.addAll(UniformDistribution.generate(random, -2 * d, 0, batch / 10));
            }
        }

        double[] values = valuesList.stream().mapToDouble(Double::doubleValue).toArray();

        return new ModalityTestData(name, values, count);
    }

    public static List<ModalityTestData> generate(Random random, String namePostfix, boolean noisy) {
        List<ModalityTestData> dataSet = new ArrayList<>();
        int maxCount = noisy ? 8 : 10;

        for (int count = 1; count <= maxCount; count++) {
            int batch = 100;
            if (noisy) {
                batch += random.nextInt(31) - 15;  // [-15, 15]
            }

            ModalityTestData testData = generateSingle(random, count, 10, 1.0, batch, namePostfix, noisy);

            if (testData != null) {
                dataSet.add(testData);
            }
        }

        return dataSet;
    }
}
