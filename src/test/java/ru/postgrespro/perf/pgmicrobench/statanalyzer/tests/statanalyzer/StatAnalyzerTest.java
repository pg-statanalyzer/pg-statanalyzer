package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.AnalysisResult;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.ModeReport;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.StatAnalyzer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgGumbelDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgLogNormalDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgWeibullDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test.
 */
public class StatAnalyzerTest {
    //TODO fix tests
    public void testStatAnalyzerWithGeneratedData() {
        List<PgSimpleDistribution> expected = new ArrayList<>();
        expected.add(new PgLogNormalDistribution(1., 0.5));
        expected.add(new PgGumbelDistribution(15.15, 3));
        expected.add(new PgWeibullDistribution(12, 45));

        List<Double> dataList = new ArrayList<>(10000);

        for (PgSimpleDistribution d : expected) {
            dataList.addAll(d.generate(3333, new Random(1)).getValues());
        }

        StatAnalyzer statAnalyzer = new StatAnalyzer();
        AnalysisResult analysisResult = statAnalyzer.analyze(dataList);
        System.out.println(analysisResult.getCompositeDistribution());

        int expectedModes = expected.size();
        assertEquals(expectedModes, analysisResult.getModeReports().size(), "Number of modes not as expected");

        for (int i = 0; i < expectedModes; i++) {
            ModeReport modeReport = analysisResult.getModeReports().get(i);
            FittedDistribution bestDistribution = modeReport.getBestDistribution();

            assertEquals(expected.get(i).getType(), bestDistribution.getDistribution().getType(),
                    "Not correct distribution " + (i + 1));

            double[] expectedParams = expected.get(i).getParamArray();
            double[] actualParams = bestDistribution.getDistribution().getParamArray();

            assertNotNull(actualParams, "Params should not be null");

            assertEquals(expectedParams[0], actualParams[0], expectedParams[0] * 0.1,
                    "First param not correct " + expected.get(i).getType());
            assertEquals(expectedParams[1], actualParams[1], expectedParams[0] * 0.1,
                    "Second param not correct " + expected.get(i).getType());
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
                if (line.isEmpty() || !Character.isDigit(line.charAt(0))) {
                    continue;
                }
                data.add(Double.parseDouble(line));
            }
        }
        return data;
    }
}
