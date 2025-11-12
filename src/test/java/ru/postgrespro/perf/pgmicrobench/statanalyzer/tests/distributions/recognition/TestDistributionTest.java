package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions.recognition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.CramerVonMises;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IDistributionTest;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Multicriteria;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test of DistributionTest algorithm.
 */
public class TestDistributionTest {
    private static final double TEST_THRESHOLD = 0.05;

    private static final List<PgSimpleDistribution> DISTRIBUTIONS = Stream.of(
            new PgLogNormalDistribution(1, 1),
            new PgLogNormalDistribution(10, 0.5),
            new PgLogNormalDistribution(13, 0.1),
            new PgLogNormalDistribution(2, 1.5),
            new PgLogNormalDistribution(14, 0.3),
            new PgLogNormalDistribution(8, 0.7),

            new PgWeibullDistribution(1, 1),
            new PgWeibullDistribution(10, 0.5),
            new PgWeibullDistribution(13, 5),
            new PgWeibullDistribution(2, 10),

            new PgFrechetDistribution(6, 1),
            new PgFrechetDistribution(10, 0.5),
            new PgFrechetDistribution(13, 5),
            new PgFrechetDistribution(5, 10),

            new PgGumbelDistribution(1, 1),
            new PgGumbelDistribution(10, 0.5),
            new PgGumbelDistribution(13, 5),
            new PgGumbelDistribution(20, 2)
    ).collect(Collectors.toUnmodifiableList());

    static Stream<Arguments> generateArgumentsForDontRejectCorrect() {
        Random random = new Random(5432);
        return Stream.of(
                new KolmogorovSmirnov(),
                new CramerVonMises(),
                new Multicriteria()
        ).map(distributionTest ->
                DISTRIBUTIONS.stream().map(d -> Arguments.of(
                        distributionTest, d,
                        d.generate(500, new Random(random.nextLong()))
                )).collect(Collectors.toUnmodifiableList())
        ).flatMap(List::stream);
    }

    static Stream<Arguments> generateArgumentsForRejectNotCorrect() {
        // Все возможные пары из DISTRIBUTIONS
        Random random = new Random(5433);
        return Stream.of(
                new KolmogorovSmirnov(),
                new CramerVonMises()
                //,new Multicriteria() // TODO fix Multicriteria
        ).flatMap(distributionTest -> DISTRIBUTIONS.stream()
                .flatMap(correctDistribution -> DISTRIBUTIONS.stream()
                        .map(distribution -> Arguments.of(
                                distributionTest,
                                distribution,
                                correctDistribution.generate(500, new Random(random.nextLong())),
                                // тест должен отвергнуть гипотезу, если инстансы распределений не равны
                                distribution != correctDistribution))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("generateArgumentsForDontRejectCorrect")
    void dontRejectCorrect(IDistributionTest distributionTest,
                               PgDistribution distribution,
                               Sample sample) {
        double test = distributionTest.test(sample, distribution);
        Assertions.assertTrue(test >= TEST_THRESHOLD);
    }

    @ParameterizedTest
    @MethodSource("generateArgumentsForRejectNotCorrect")
    void rejectNotCorrect(IDistributionTest distributionTest,
                          PgDistribution distribution,
                          Sample sample,
                          boolean toReject) {
        double test = distributionTest.test(sample, distribution);
        if (toReject) {
            Assertions.assertTrue(test < TEST_THRESHOLD);
        } else {
            Assertions.assertTrue(test >= TEST_THRESHOLD);
        }
    }
}
