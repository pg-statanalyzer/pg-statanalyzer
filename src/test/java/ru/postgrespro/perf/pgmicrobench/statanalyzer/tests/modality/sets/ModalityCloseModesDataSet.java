package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.sets;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgUniformDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * Utility class for generating datasets with modality test data,
 * specifically focused on testing close modes in distributions.
 */

public class ModalityCloseModesDataSet {

    /**
     * Generates single {@link ModalityTestData} instance with close modes.
     * Data is built by generating two uniform distributions and applying transformations.
     *
     * @param random      instance of {@link Random} for generating random numbers.
     * @param delta       shift applied to mode values to create close modes.
     * @param batch       number of values to generate for each mode.
     * @param namePostfix optional postfix to append to dataset name for identification.
     * @return a {@link ModalityTestData} instance containing generated dataset.
     */
    private static ModalityTestData generateSingle(Random random,
                                                   double delta,
                                                   int batch,
                                                   String namePostfix) {
        String name = String.format("CloseModes(delta = %.2f, batch=%d)%s",
                delta,
                batch,
                namePostfix);

        List<Double> values = new ArrayList<>();
        values.addAll(PgUniformDistribution.generate(random, 0, 1, batch).getValues()
                .stream().map(x -> delta + Math.pow(x, 3)).collect(Collectors.toList()));
        values.addAll(PgUniformDistribution.generate(random, 0, 1, batch).getValues()
                .stream().map(x -> -delta - Math.pow(x, 3)).collect(Collectors.toList()));

        double[] valuesArray = values.stream().mapToDouble(Double::doubleValue).toArray();

        return new ModalityTestData(name, valuesArray, 2);
    }

    /**
     * Generates list of {@link ModalityTestData} datasets with different deltas for testing close modes.
     * Each dataset is generated with batch size of 1000 values for two modes.
     *
     * @param random      instance of {@link Random} for generating random numbers.
     * @param namePostfix optional postfix to append to dataset names for identification.
     * @return list of {@link ModalityTestData} instances representing generated datasets.
     */
    public static List<ModalityTestData> generate(Random random, String namePostfix) {
        List<ModalityTestData> dataSet = new ArrayList<>();

        int batchSize = 1000;
        dataSet.add(generateSingle(random, 0.5, batchSize, namePostfix));
        dataSet.add(generateSingle(random, 0.1, batchSize, namePostfix));
        dataSet.add(generateSingle(random, 0.01, batchSize, namePostfix));

        return dataSet;
    }
}
