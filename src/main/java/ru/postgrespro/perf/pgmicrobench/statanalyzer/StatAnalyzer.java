package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov;

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

    public static void main(String[] args) {
        String file = "distributionSample/lognorm_13_07";

        List<Double> dataList = new ArrayList<>(10000);
        try (Scanner scanner = new Scanner(new File(file))) {
            scanner.nextLine();
            scanner.nextLine();

            while (scanner.hasNextDouble()) {
                dataList.add(scanner.nextDouble());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

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

            System.out.println();
        }
    }
}
