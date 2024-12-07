package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Application class.
 */
public class StatAnalyzer {
    static final PgDistributionType[] supportedDistributions = PgDistributionType.values();
    private static final LowlandModalityDetector detector = new LowlandModalityDetector(0.5, 0.01, false);

    public static void main(String[] args) {
        String file = "distributionSample/SELECT.csv";

        List<Double> dataList = new ArrayList<>(10000);
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextDouble()) {
                dataList.add(scanner.nextDouble() / 100000.0); // division to simplify the search for parameters
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Sample sample = new Sample(dataList, true);

        ModalityData result = detector.detectModes(sample);

        System.out.println("Detected modality: " + result.getModality());

        for (RangedMode mode : result.getModes()) {
            System.out.println("Processing mode: " + mode);

            Sample modeData = new Sample(dataList.stream()
                    .filter(value -> value >= mode.getLeft() && value <= mode.getRight())
                    .collect(Collectors.toList()), false);

            System.out.println("Data size in this mode: " + modeData.size());

            Sample paramsSample = new Sample(
                    IntStream.iterate(0, n -> n + 2).limit((modeData.size() + 1) / 2)
                            .mapToObj(modeData::get)
                            .collect(Collectors.toList()));
            Sample testSample = new Sample(
                    IntStream.iterate(1, n -> n + 2).limit(modeData.size() / 2)
                            .mapToObj(modeData::get)
                            .collect(Collectors.toList()));

            KolmogorovSmirnov kolmogorovSmirnov = new KolmogorovSmirnov();
            for (PgDistributionType distributionType : supportedDistributions) {
                System.out.println("Fitting distribution: " + distributionType.name());
                FittedDistribution fd;
                try {
                    fd = kolmogorovSmirnov.fit(paramsSample, distributionType);
                } catch (Exception e) {
                    System.out.println("Cant find parameters: " + distributionType.name());
                    System.out.println();
                    continue;
                }

                Plot.plot(modeData, fd.getDistribution()::pdf, distributionType.name() + " (Mode)");

                System.out.println("Params: " + Arrays.toString(fd.getParams()));

                double pValue = kolmogorovSmirnov.test(testSample, fd.getDistribution());
                System.out.println("pValue: " + pValue);

                System.out.println();
            }
        }
    }
}
