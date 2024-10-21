package ru.postgrespro.perf.pgmicrobench.statanalyzer.Tests.TestDataSets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModalityTestData {
    private final String name;
    private final List<Double> values;
    private final int expectedModality;

    public ModalityTestData(String name, double[] values, int expectedModality) {
        this.name = name;
        this.values = Collections.unmodifiableList(Arrays.asList(
                Arrays.stream(values).boxed().toArray(Double[]::new)
        ));
        this.expectedModality = expectedModality;
    }

    public String getName() {
        return name;
    }

    public List<Double> getValues() {
        return values;
    }

    public int getExpectedModality() {
        return expectedModality;
    }

    @Override
    public String toString() {
        return String.format("f.Tests.Modality.TestDataSets.ModalityTestData{name='%s', expectedModality=%d, values=%s}",
                name, expectedModality, values);
    }
}
