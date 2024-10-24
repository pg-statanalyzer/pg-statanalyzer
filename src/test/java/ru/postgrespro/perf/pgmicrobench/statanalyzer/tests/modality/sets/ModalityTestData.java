package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.sets;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * {@code ModalityTestData} class represents dataset for modality testing.
 * It contains name of dataset, list of numerical values, and expected modality of data.
 */

@Getter
public class ModalityTestData {
    private final String name;
    private final List<Double> values;
    private final int expectedModality;

    /**
     * Constructs new {@code ModalityTestData} instance.
     *
     * @param name             name of dataset
     * @param values           array of double values representing dataset
     * @param expectedModality expected modality of dataset (e.g., unimodal, bimodal)
     */
    public ModalityTestData(String name, double[] values, int expectedModality) {
        this.name = name;
        this.values = Collections.unmodifiableList(Arrays.asList(
                Arrays.stream(values).boxed().toArray(Double[]::new)
        ));
        this.expectedModality = expectedModality;
    }

    /**
     * Returns string representation of {@code ModalityTestData} instance,
     * including name, expected modality and values in dataset.
     *
     * @return string representation of modality test data
     */
    @Override
    public String toString() {
        return String.format("f.test.modality.dataSets.ModalityTestData{name='%s', expectedModality=%d, values=%s}",
                name,
                expectedModality,
                values);
    }
}
