package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.dataSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModalityReferenceDataSet {

    public static List<ModalityTestData> generate(Random random) {
        return generate(random, 10);
    }

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

    private static String getNamePostfix(int i, int repetitions) {
        return repetitions == 1 ? "" : "@" + i;
    }
}
