package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.GumbelDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.QuantileRespectfulDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.dataSets.ModalityReferenceDataSet;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.dataSets.ModalityTestData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModalityDetectorTests2 {
    private final LowlandModalityDetector detector = new LowlandModalityDetector(0.5, 0.01, false);
    private static final List<ModalityTestData> referenceDataSet = ModalityReferenceDataSet.generate(new Random(42), 5);

    @BeforeEach
    void setup() {
        System.out.println("Setting up the test...");
    }

    @ParameterizedTest
    @MethodSource("provideReferenceDataSetNames")
    @DisplayName("Test with reference data set")
    void referenceDataSetTest(String name) {
        System.out.println("Case: " + name);
        ModalityTestData modalityTestData = referenceDataSet.stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Test data not found for name: " + name));

        int expectedModality = modalityTestData.getExpectedModality();

        ModalityData modalityData = detector.detectModes(
                new Sample(modalityTestData.getValues()),
                QuantileRespectfulDensityHistogramBuilder.getInstance()
        );

        Assertions.assertNotNull(modalityData, "Failed to get ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData from DetectModes");

        int actualModality = modalityData.getModality();

        System.out.println("ActualModality   : " + actualModality);
        System.out.println("ExpectedModality : " + expectedModality);
        System.out.println("-----");
        System.out.println("Modes:");
        System.out.println(modalityData);
        System.out.println("-----");

        assertEquals(expectedModality, actualModality);
    }

    @DisplayName("Weighted ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample Test")
    @Test
    public void weightedSampleTest() {
        Random random = new Random(42);

        List<Double> values = IntStream.range(0, 30)
                .mapToDouble(i -> new GumbelDistribution().random(random))
                .boxed()
                .collect(Collectors.toList());

        values.addAll(IntStream.range(0, 30)
                .mapToDouble(i -> new GumbelDistribution(10).random(random))
                .boxed()
                .collect(Collectors.toList()));

        List<Double> weights = IntStream.range(0, values.size())
                .mapToDouble(i -> Math.exp(-0.1 * i))
                .boxed()
                .collect(Collectors.toList());
        Sample sample = new Sample(values, weights);

        ModalityData simpleModalityData = detector.detectModes(new Sample(values));
        System.out.println("SimpleModalityData.Modes:");
        System.out.println(simpleModalityData);
        System.out.println();
        System.out.println(simpleModalityData.getDensityHistogram().present("N2", Locale.US));
        System.out.println("------------------------------");

        ModalityData weightedModalityData = detector.detectModes(sample);
        System.out.println("WeightedModalityData.Modes:");
        System.out.println(weightedModalityData);
        System.out.println();
        System.out.println(weightedModalityData.getDensityHistogram().present("N2", Locale.US));

        Assertions.assertEquals(2, simpleModalityData.getModality());
        Assertions.assertEquals(1, weightedModalityData.getModality());
    }

    private static Stream<String> provideReferenceDataSetNames() {
        return referenceDataSet.stream().map(ModalityTestData::getName);
    }
}
