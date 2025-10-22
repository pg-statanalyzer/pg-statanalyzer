package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions.recognition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.StatAnalyzerTestUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Test all parameter estimating algorithm.
 */
public class TestParameterEstimator {

    private static final List<PgSimpleDistribution> distributions = Stream.of(
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
    ).toList();

    static Stream<Arguments> provideParameters() {
        Random random = new Random(5431);
        return Stream.of(
                new MaximumLikelihoodEstimation(),
                new KolmogorovSmirnov(),
                new CramerVonMises()
                //new Multicriteria()
        ).map(estimator ->
                distributions.stream().map(d -> Arguments.of(
                        estimator, d,
                        d.generate(2000, new Random(random.nextLong()))
                )).toList()
        ).flatMap(List::stream);

    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void testParameterEstimator(IParameterEstimator parameterEstimator,
                                PgSimpleDistribution distribution, Sample sample) {
        PgSimpleDistribution startDistribution = distribution.newDistribution(sample);

        // We don't support samples with huge Mean or Variance
        Assertions.assertTrue(sample.getMean() < 1e12);
        Assertions.assertTrue(sample.getVariance() < 1e12);

        EstimatedParameters estimatedParameters = parameterEstimator.fit(sample, startDistribution);

        Assertions.assertTrue(StatAnalyzerTestUtils.isDistributionsEqual(
                distribution,
                estimatedParameters.getDistribution(),
                0.05), "Expected: " + distribution + " Real: " + estimatedParameters.getDistribution());
    }
}
