package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.CramerVonMises;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.EstimatedParameters;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test.
 */
public class SimpleFittingTest {
    private static final LowlandModalityDetector detector = new LowlandModalityDetector(0.5, 0.01, false);

    private static final double[][] EXPECTED_PARAMS = {
            {4.278, 0.01}, // lognorm_4278_001
            {10.0, 0.3},   // gamma_10_03
            {3.0, 0.3},    // lognorm_3_0.3
            {1.3, 0.7},    // lognorm_13_07
    };

    @Test
    public void testLogNorm4278() throws IOException {
        testSingleSample("distributionSample/lognorm_4278_001", PgDistributionType.LOGNORMAL, EXPECTED_PARAMS[0]);
    }

    //    @Test
    //    public void testGamma1003() throws IOException {
    //        testSingleSample("distributionSample/gamma_10_03", PgDistributionType.WEIBULL, EXPECTED_PARAMS[1]);
    //    }

    @Test
    public void testLogNorm303() throws IOException {
        testSingleSample("distributionSample/lognorm_3_0.3", PgDistributionType.LOGNORMAL, EXPECTED_PARAMS[2]);
    }

    @Test
    public void testLogNorm1307() throws IOException {
        testSingleSample("distributionSample/lognorm_13_07", PgDistributionType.LOGNORMAL, EXPECTED_PARAMS[3]);
    }

    private void testSingleSample(String filePath, PgDistributionType distributionType, double[] expectedParams) throws IOException {
        List<Double> sampleData = readSampleFromFile(filePath);
        Sample sample = new Sample(sampleData, true);
        //Plot.plot(sample);

        ModalityData result = detector.detectModes(sample);

        assertEquals(1, result.getModes().size(), "Should be one modality");

        RangedMode mode = result.getModes().get(0);

        Sample modeData = new Sample(sampleData.stream()
                .filter(value -> value >= mode.getLeft() && value <= mode.getRight())
                .collect(Collectors.toList()), false);

        assertTrue(modeData.size() > 100, "Not enough data");

        findAndVerifyDistribution(modeData, distributionType, expectedParams);
    }

    private List<Double> readSampleFromFile(String filePath) throws IOException {
        List<Double> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine();
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                data.add(Double.parseDouble(line.trim()));
            }
        }
        return data;
    }

    private void findAndVerifyDistribution(Sample modeData, PgDistributionType distributionType, double[] expectedParams) {
        CramerVonMises cramerVonMises = new CramerVonMises();

        try {
            EstimatedParameters fittedDistribution = cramerVonMises.fit(modeData, distributionType);
            double pValue = cramerVonMises.test(modeData, fittedDistribution.getDistribution());

            System.out.println("Testing distribution: " + distributionType.name());
            System.out.println("Fitted params: " + Arrays.toString(fittedDistribution.getParams()));
            System.out.println("pValue: " + pValue);

            assertTrue(pValue > 0.05, "bad pValue");

            assertEquals(expectedParams[0], fittedDistribution.getParams()[0], 0.4, "Среднее не совпадает");
            assertEquals(expectedParams[1], fittedDistribution.getParams()[1], 0.4, "Стандартное отклонение не совпадает");
        } catch (Exception e) {
            fail("Can not fit distribution: " + distributionType.name());
        }
    }
}
