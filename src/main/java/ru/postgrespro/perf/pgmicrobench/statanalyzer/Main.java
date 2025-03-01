package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String file = "distributionSample/twoMode.csv";

        int skip = 0;
        List<Double> dataList = new ArrayList<>(300000);
        try (Scanner scanner = new Scanner(new File(file))) {
            scanner.nextLine();
            scanner.nextLine();
            while (scanner.hasNextDouble()) {
                double value = scanner.nextDouble(); // division to simplify the search for parameters
                if (skip++ % 10 == 0) {

                    dataList.add(value);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Sample sample = new Sample(dataList);

        StatAnalyzer statAnalyzer = new StatAnalyzer();

        AnalysisResult analysisResult = statAnalyzer.analyze(dataList);

        PgCompositeDistribution compositeDistribution = analysisResult.compositeDistribution;

        Plot.plot(sample, compositeDistribution::pdf, "Analyze result");

        System.out.println(compositeDistribution);
    }
}
