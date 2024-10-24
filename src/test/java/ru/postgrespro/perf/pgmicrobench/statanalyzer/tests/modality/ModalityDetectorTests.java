package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.modality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.UniformDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.loader.Loader;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.jfree.data.statistics.HistogramDataset;


public class ModalityDetectorTests {

    private Loader analyzer;
    private LowlandModalityDetector detector;

    @BeforeEach
    public void setup() {
        analyzer = new Loader();
        detector = new LowlandModalityDetector(0.5, 0.01, false);
    }

    @Test
    public void testLatencyLoading() {
        double[] latencies = generateNormalDistribution(10000, 5.0, 1.0)
                .stream().mapToDouble(Double::doubleValue).toArray();
        analyzer.loadLatencies(latencies);

        Assertions.assertEquals(10000, analyzer.getLatencyCount(), "Incorrect latency count.");
    }

    @Test
    public void testModalityDetectionWithMixedDistributions() {
        List<Double> values = new ArrayList<>();
        values.addAll(generateNormalDistribution(10000, 5.0, 1.0));
        values.addAll(generateUniformDistribution(10000, 10.0, 15.0));

        Sample sample = new Sample(values);
        ModalityData result = detector.detectModes(sample);

        int expectedModality = 2;
        Assertions.assertEquals(expectedModality, result.getModality(), "Incorrect modality detected.");

        List<Double> modeLocations = result.getModes().stream()
                .map(RangedMode::getLocation)
                .collect(Collectors.toList());

        assertTrue(modeLocations.stream().anyMatch(m -> m >= 4.5 && m <= 5.5),
                "Mode near 5 not detected.");
        assertTrue(modeLocations.stream().anyMatch(m -> m >= 10 && m <= 15),
                "Mode in uniform range not detected.");
    }

    private List<Double> generateNormalDistribution(int size, double mean, double stdDev) {
        Random random = new Random();
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(mean + stdDev * random.nextGaussian());
        }
        return data;
    }

    private List<Double> generateUniformDistribution(int size, double min, double max) {
        Random random = new Random();
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(min + (max - min) * random.nextDouble());
        }
        return data;
    }

    @Test
    public void testHistogramDatasetCreation() {
        List<Double> values = generateNormalDistribution(1000, 5.0, 1.0);
        HistogramDataset dataset = createHistogramDataset(values, 20);

        assertEquals(1, dataset.getSeriesCount(), "Dataset should contain one series.");
        assertTrue(dataset.getItemCount(0) > 0, "Dataset should contain bins.");
    }

    private HistogramDataset createHistogramDataset(List<Double> values, int bins) {
        HistogramDataset dataset = new HistogramDataset();
        double[] data = values.stream().mapToDouble(Double::doubleValue).toArray();
        dataset.addSeries("Frequency", data, bins);
        return dataset;
    }
}