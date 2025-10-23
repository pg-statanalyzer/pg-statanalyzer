package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.AnalysisResult;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.ModeReport;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.StatAnalyzer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Test.
 */
public class TestStatAnalyzer {

    @Test
    public void testGeneratedSimpleDistributions() {
        CompletableFuture.allOf(IntStream.range(0, 10).mapToObj(seed ->
                CompletableFuture.runAsync(() -> {
                    for (SampleTarget<PgSimpleDistribution> sampleTarget :
                            StatAnalyzerTestUtils.getSimpleSampleTargets(5000, new Random(seed))) {
                        StatAnalyzer statAnalyzer = StatAnalyzer.builder()
                                .random(new Random(seed + 1))
                                .build();

                        AnalysisResult analysisResult = statAnalyzer.analyze(sampleTarget.sample.getValues());

                        Assertions.assertEquals(1, analysisResult.getModeReports().size(),
                                "Number of modes not as expected");

                        ModeReport modeReport = analysisResult.getModeReports().get(0);
                        FittedDistribution bestDistribution = modeReport.getBestDistribution();

                        Assertions.assertTrue(StatAnalyzerTestUtils.isDistributionsEqual(
                                        bestDistribution.getDistribution(),
                                        sampleTarget.target, 0.10), // should improve algorithms to decrease ratio
                                "Expected: " + sampleTarget.target + " Real: " + bestDistribution.getDistribution());
                    }
                })
        ).toArray(CompletableFuture[]::new)).join();
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
