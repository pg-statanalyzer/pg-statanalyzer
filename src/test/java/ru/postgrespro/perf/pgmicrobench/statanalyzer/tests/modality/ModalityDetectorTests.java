package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality;

import org.jfree.data.statistics.HistogramDataset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgNormalDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgUniformDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.loader.Loader;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@code LowlandModalityDetector} and related functionalities.
 * This class tests latency loading, modality detection and histogram dataset creation.
 */

public class ModalityDetectorTests {

    private Loader analyzer;
    private LowlandModalityDetector detector;

    /**
     * Sets up test environment by initializing {@code Loader} and {@code LowlandModalityDetector}
     * before each test case.
     */
    @BeforeEach
    public void setup() {
        analyzer = new Loader();
        detector = new LowlandModalityDetector(0.5, 0.01, false);
    }

    /**
     * Tests loading of latencies into {@code Loader}.
     * Verifies that correct number of latencies is loaded.
     */
    @Test
    public void testLatencyLoading() {
        List<Double> latencies = PgNormalDistribution.generate(10000, 5.0, 1.0);
        analyzer.loadLatencies(latencies);

        Assertions.assertEquals(10000,
                analyzer.getLatencyCount(),
                "Incorrect latency count.");
    }

    /**
     * Tests modality detection with mixed distributions.
     * This test generates sample combining normal and uniform distribution,
     * and checks if detector correctly identifies modality and detects modes in specified ranges.
     */
    @Test
    public void testModalityDetectionWithMixedDistributions() {
        List<Double> values = new ArrayList<>();

        values.addAll(PgNormalDistribution.generate(10000, 5.0, 1.0));

        values.addAll(PgUniformDistribution.generate(new Random(),
                10.0,
                15.0,
                10000));

        Sample sample = new Sample(values);
        ModalityData result = detector.detectModes(sample);

        int expectedModality = 2;
        Assertions.assertEquals(expectedModality,
                result.getModality(),
                "Incorrect modality detected.");

        List<Double> modeLocations = result.getModes().stream()
                .map(RangedMode::getLocation)
                .collect(Collectors.toList());

        assertTrue(modeLocations.stream().anyMatch(m -> m >= 4.5 && m <= 5.5),
                "Mode near 5 not detected.");
        assertTrue(modeLocations.stream().anyMatch(m -> m >= 10 && m <= 15),
                "Mode in uniform range not detected.");
    }

    /**
     * Tests creation of histogram dataset from generated normal distribution values.
     * Verifies that dataset contains one series and that it has bins.
     */
    @Test
    public void testHistogramDatasetCreation() {
        List<Double> values = PgNormalDistribution.generate(1000, 5.0, 1.0);
        HistogramDataset dataset = createHistogramDataset(values, 20);

        assertEquals(1,
                dataset.getSeriesCount(),
                "Dataset should contain one series.");
        assertTrue(dataset.getItemCount(0) > 0,
                "Dataset should contain bins.");
    }

    /**
     * Creates histogram dataset from list of values.
     *
     * @param values values to be included in dataset
     * @param bins   number of bins to create in histogram
     * @return {@code HistogramDataset} containing specified values and bins
     */
    private HistogramDataset createHistogramDataset(List<Double> values, int bins) {
        HistogramDataset dataset = new HistogramDataset();
        double[] data = values.stream().mapToDouble(Double::doubleValue).toArray();
        dataset.addSeries("Frequency", data, bins);
        return dataset;
    }
}
