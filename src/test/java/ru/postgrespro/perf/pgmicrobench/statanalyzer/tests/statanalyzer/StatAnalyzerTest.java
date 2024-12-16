package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.AnalysisResult;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.ModeReport;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.StatAnalyzer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.util.*;
import java.io.*;

public class StatAnalyzerTest {

    private static final String TEST_FILE = "sampleGenerator/multi_for_test.txt";
    private static final double[][] EXPECTED_PARAMS = {
            {1.0, 1.7},  // weibull
            {8.88, 1.1}, // normal
            {3.15, 0.07},  // lognormal
    };

    private static final PgDistributionType[] DISTRIBUTION_ORDER = {
            PgDistributionType.WEIBULL,
            PgDistributionType.NORMAL,
            PgDistributionType.LOGNORMAL,
    };

    @Test
    public void testStatAnalyzerWithGeneratedData() throws IOException, InterruptedException {
        //generateTestData();

        List<Double> dataList = readSampleFromFile(TEST_FILE);

        //Sample sample = new Sample(dataList, true);
        //Plot.plot(sample);

        StatAnalyzer statAnalyzer = new StatAnalyzer();
        AnalysisResult analysisResult = statAnalyzer.analyze(dataList);
        Plot.plot(new Sample(dataList), analysisResult.getPdf(), "Summary pdf");
        Thread.sleep(2000);

        int expectedModes = EXPECTED_PARAMS.length;
        assertEquals(expectedModes, analysisResult.getModeReports().size(), "Number of modes not as expected");

        for (int i = 0; i < expectedModes; i++) {
            ModeReport modeReport = analysisResult.getModeReports().get(i);
            FittedDistribution bestDistribution = modeReport.getBestDistribution();

            assertEquals(DISTRIBUTION_ORDER[i], bestDistribution.getType(),
                    "Not correct distribution " + (i + 1));

            double[] expectedParams = EXPECTED_PARAMS[i];
            double[] actualParams = bestDistribution.getParameters();

            assertNotNull(actualParams, "Params should not be null");

            assertEquals(expectedParams[0], actualParams[0], 0.2,
                    "First param not correct " + DISTRIBUTION_ORDER[i]);
            assertEquals(expectedParams[1], actualParams[1], 0.2,
                    "Second param not correct " + DISTRIBUTION_ORDER[i]);
        }

        System.out.println("Result: ");
        analysisResult.getModeReports().forEach(System.out::println);
    }

    private void generateTestData() {
        try {
            System.out.println("Запуск Python-генератора данных...");
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "sampleGenerator/sampleGenerator.py");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Error while processing generator " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem with generator", e);
        }
    }

    private List<Double> readSampleFromFile(String filePath) throws IOException {
        List<Double> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace(",", ".").trim();
                if (line.isEmpty() || !Character.isDigit(line.charAt(0))) continue;
                data.add(Double.parseDouble(line));
            }
        }
        return data;
    }
}
