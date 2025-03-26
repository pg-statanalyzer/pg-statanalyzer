package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main class.
 */
public class Main {
    public static void main(String[] args) {
        String file = "distributionSample/postgres-16.8";

        List<Double> dataList = new ArrayList<>(300000);
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                dataList.add(value);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Sample sample = new Sample(dataList);

        StatAnalyzer statAnalyzer = new StatAnalyzer();
        statAnalyzer.setUseJittering(true);
        statAnalyzer.setRecursiveModeDetection(true);

        AnalysisResult analysisResult = statAnalyzer.analyze(dataList);

        PgCompositeDistribution compositeDistribution = analysisResult.compositeDistribution;

        System.out.println(compositeDistribution);
        Plot.plot(sample, compositeDistribution::pdf, "Final");
    }
}
