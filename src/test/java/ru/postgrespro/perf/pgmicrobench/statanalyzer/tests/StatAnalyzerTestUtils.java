package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Test utils.
 */
public class StatAnalyzerTestUtils {

    /**
     * Compares two distributions for equality within a specified ratio.
     *
     * <p>This method checks if two distributions of the same type are equal by comparing their parameters.
     * The comparison is conducted in a way that allows for a specified ratio of difference between
     * the parameters of the two distributions. If both distributions are null, they are considered equal.</p>
     *
     * @param d1 the first distribution to compare; may be null
     * @param d2 the second distribution to compare; may be null
     * @param ratio the maximum allowable ratio of difference between corresponding parameters;
     *               should be greater than or equal to 0
     * @return {@code true} if the distributions are considered equal within the given ratio,
     *         {@code false} otherwise
     */
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

    /**
     * Generate test arguments.
     *
     * @param size size.
     * @param random random.
     * @return list.
     */
    static public List<SampleTarget<PgSimpleDistribution>> getSimpleSampleTargets(int size, Random random) {
        // TODO use parametrized test
        return Stream.of(
                        new PgLogNormalDistribution(2, 2),
                        new PgGumbelDistribution(2, 2),
                        new PgFrechetDistribution(2, 2),
                        new PgWeibullDistribution(2, 2)
                ).map(it -> new SampleTarget<>(it.generate(size, random), it))
                .toList();
    }
}
