package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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

        Sample sample = new Sample(dataList);

        ModalityData result = detector.detectModes(sample);

        System.out.println("Detected modality: " + result.getModality());

        result.getModes().forEach(mode -> {
            System.out.println(mode.toString());
        });

        double[] params = new double[dataList.size() / 2 + (dataList.size() & 1)];
        double[] test = new double[dataList.size() / 2];

        for (int i = 0; i < dataList.size(); i++) {
            if ((i & 1) == 0) {
                params[i / 2] = dataList.get(i);
            } else {
                test[i / 2] = dataList.get(i);
            }
        }

        for (PgDistributionType distributionType : supportedDistributions) {
            System.out.println(distributionType.name() + ":");
            FittedDistribution fd;
            try {
                fd = KolmogorovSmirnov.fit(params, new double[]{1, 1}, distributionType);
            } catch (Exception e) {
                System.out.println("Cant find parameters");
                System.out.println();
                continue;
            }
            System.out.println("Params: " + Arrays.toString(fd.getParams()));
            double pValue = KolmogorovSmirnov.ksTest(test, fd.getDistribution());
            System.out.println("pValue: " + pValue);


            Plot.plot(dataList, fd.getDistribution()::pdf, distributionType.name());

            System.out.println();
        }
    }
}
