package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgFrechetDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgLogNormalDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main class.
 */
public class Main {
    public static void main(String[] args) {
        String file = "distributionSample/twoMode.csv";

        List<Double> dataList = new ArrayList<>(30000);
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextDouble() && dataList.size() < 10000) {
                double value = scanner.nextDouble();
                dataList.add(value);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        PgCompositeDistribution d = new PgCompositeDistribution(
                List.of(
                        new PgLogNormalDistribution(1.0, 0.5),
                        new PgFrechetDistribution(12.0, 6.5)
                ), List.of(0.7, 0.3)
        );

        Sample s = d.generate(10000, new Random(11));

        Sample sample = new Sample(dataList);


        StatAnalyzer statAnalyzer = StatAnalyzer.builder().build();
        Statalt statalt = Statalt.builder().build();

        AnalysisResult analysisResult = statAnalyzer.analyze(s.getValues());

        PgCompositeDistribution compositeDistribution = analysisResult.compositeDistribution;

        System.out.println(compositeDistribution);
        Plot.plot(s, compositeDistribution::pdf, "Final");
    }
}
