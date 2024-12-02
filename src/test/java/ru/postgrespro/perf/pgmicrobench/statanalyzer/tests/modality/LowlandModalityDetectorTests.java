package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgGumbelDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.QuantileRespectfulDensityHistogramBuilder;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.sets.ModalityReferenceDataSet;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality.sets.ModalityTestData;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for {@code LowlandModalityDetector} class.
 * This class tests modality detection using reference datasets and weighted samples.
 */

class LowlandModalityDetectorTests {

    private final LowlandModalityDetector detector = new LowlandModalityDetector(0.5,
            0.01,
            false);

    private static final List<ModalityTestData> referenceDataSet = ModalityReferenceDataSet
            .generate(new Random(42),
                    5);

    /**
     * Sets up test environment before each test case.
     */
    @BeforeEach
    void setup() {
        System.out.println("Setting up the test...");
    }

    /**
     * Parameterized test that runs modality detection on set of reference data.
     *
     * @param name name of modality test data to be used in test
     */
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
                new Sample(modalityTestData.getValues(), true),
                QuantileRespectfulDensityHistogramBuilder.getInstance()
        );

        Assertions.assertNotNull(modalityData,
                "Failed to get ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData from DetectModes");

        int actualModality = modalityData.getModality();

        System.out.println("ActualModality   : " + actualModality);
        System.out.println("ExpectedModality : " + expectedModality);
        System.out.println("-----");
        System.out.println("Modes:");
        System.out.println(modalityData);
        System.out.println("-----");

        assertEquals(expectedModality, actualModality);
    }

    /**
     * Tests modality detection with weighted sample.
     * Generates two sets of values from Gumbel distributions and applies weights.
     * Compares modality detection results between simple and weighted samples.
     */
    @DisplayName("Weighted ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample Test")
    @Test
    public void weightedSampleTest() {
        Random random = new Random(42);

        List<Double> values = IntStream.range(0, 30)
                .mapToDouble(i -> new PgGumbelDistribution().random(random))
                .boxed()
                .collect(Collectors.toList());

        values.addAll(IntStream.range(0, 30)
                .mapToDouble(i -> new PgGumbelDistribution(10).random(random))
                .boxed()
                .collect(Collectors.toList()));

        List<Double> weights = IntStream.range(0, values.size())
                .mapToDouble(i -> Math.exp(-0.1 * i))
                .boxed()
                .collect(Collectors.toList());
        Sample sample = new Sample(values, weights);

        ModalityData simpleModalityData = detector.detectModes(new Sample(values, true));
        System.out.println("SimpleModalityData.Modes:");
        System.out.println(simpleModalityData);
        System.out.println();
        System.out.println(simpleModalityData.getDensityHistogram().present("N2",
                Locale.US));
        System.out.println("------------------------------");

        ModalityData weightedModalityData = detector.detectModes(sample);
        System.out.println("WeightedModalityData.Modes:");
        System.out.println(weightedModalityData);
        System.out.println();
        System.out.println(weightedModalityData.getDensityHistogram().present("N2",
                Locale.US));

        assertEquals(2, simpleModalityData.getModality());
        assertEquals(1, weightedModalityData.getModality());
    }

    /**
     * Provides names of reference data sets for parameterized testing.
     *
     * @return stream of names from reference data set
     */
    private static Stream<String> provideReferenceDataSetNames() {
        return referenceDataSet.stream().map(ModalityTestData::getName);
    }
}
