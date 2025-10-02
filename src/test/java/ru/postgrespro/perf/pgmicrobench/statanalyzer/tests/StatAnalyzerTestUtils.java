package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class StatAnalyzerTestUtils {

    public static boolean isDistributionsEqual(PgDistribution d1, PgDistribution d2, double ratio) {
        if (d1 == null && d2 == null) {
            return true;
        }

        if (d1 == null || d2 == null) {
            return false;
        }

        if (d1.getClass() != d2.getClass()) {
            return false;
        }

        for (int i = 0; i < d1.getParamNumber(); i++) {
            double p1 = Math.abs(d1.getParamArray()[i]);
            double p2 = Math.abs(d2.getParamArray()[i]);

            double diff = Math.abs(p1 - p2);
            double greatest = Math.max(p1, p2);

            if (greatest < 0.00001) {
                continue;
            }

            if (diff / greatest > ratio) {
                return false;
            }
        }

        return true;
    }

    static public List<SampleTarget<PgSimpleDistribution>> getSimpleSampleTargets(int size, Random random) {
        return Stream.of(
//                        new PgNormalDistribution(2, 2),
                        new PgLogNormalDistribution(2, 2),
                        new PgGumbelDistribution(2, 2),
                        new PgFrechetDistribution(2, 2),
                        new PgWeibullDistribution(2, 2)
                ).map(it -> new SampleTarget<>(it.generate(size, random), it))
                .toList();
    }
}
