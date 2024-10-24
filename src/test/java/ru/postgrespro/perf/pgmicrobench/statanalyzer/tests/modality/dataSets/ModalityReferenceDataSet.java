package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.dataSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * {@code ModalityReferenceDataSet} class is responsible for generating composite dataset
 * of modality test data by combining manual data, Gumbel location progression data,
 * and close modes data.
 */

public class ModalityReferenceDataSet {

    /**
     * Generates list of modality test data using specified random generator.
     * By default, it repeats generation process 10 times.
     *
     * @param random {@link Random} instance used for generating random values
     * @return list of {@link ModalityTestData} containing generated datasets
     */
    public static List<ModalityTestData> generate(Random random) {
        return generate(random, 10);
    }

    /**
     * Generates list of modality test data using specified random generator
     * and number of repetitions for generating datasets.
     *
     * @param random      {@link Random} instance used for generating random values
     * @param repetitions number of times to repeat generation
     * @return list of {@link ModalityTestData} containing generated datasets
     */
    public static List<ModalityTestData> generate(Random random, int repetitions) {
        List<ModalityTestData> dataSet = new ArrayList<>(ModalityManualDataSet.ALL);

        for (int i = 0; i < repetitions; i++) {
            dataSet.addAll(ModalityGumbelLocationProgressionDataSet.generate(random, getNamePostfix(i, repetitions), false));
        }

        for (int i = 0; i < repetitions; i++) {
            dataSet.addAll(ModalityGumbelLocationProgressionDataSet.generate(random, getNamePostfix(i, repetitions), true));
        }

        for (int i = 0; i < repetitions; i++) {
            dataSet.addAll(ModalityCloseModesDataSet.generate(random, getNamePostfix(i, repetitions)));
        }

        return dataSet;
    }

    /**
     * Generates name postfix based on current iteration and total number of repetitions.
     * If there is only one repetition, empty string is returned; otherwise, format "@i" is used.
     *
     * @param i           current iteration index
     * @param repetitions total number of repetitions
     * @return a {@link String} representing name postfix
     */
    private static String getNamePostfix(int i, int repetitions) {
        return repetitions == 1 ? "" : "@" + i;
    }
}
