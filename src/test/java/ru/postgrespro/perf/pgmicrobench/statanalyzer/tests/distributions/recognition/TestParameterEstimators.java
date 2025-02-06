package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions.recognition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.*;

import java.util.Random;
import java.util.stream.Stream;

public class TestParameterEstimators {
    static Stream<Arguments> parameterEstimators() {
        return Stream.of(
                Arguments.of(new CramerVonMises()),
                Arguments.of(new KolmogorovSmirnov()),
                Arguments.of(new Multicriteria()),
                Arguments.of(new MaximumLikelihoodEstimation())
        );
    }

    @ParameterizedTest
    @MethodSource("parameterEstimators")
    void testNormal(IParameterEstimator estimator) {
        PgDistribution distribution = new PgNormalDistribution(2, 2);

        for (long seed = 0; seed < 10; seed++) {
            Random rand = new Random(seed);
            Sample sample = distribution.generate(10000, rand);

            EstimatedParameters ep = estimator.fit(sample, PgDistributionType.NORMAL);

            Assertions.assertEquals(2, ep.params[0], 5e-2);
            Assertions.assertEquals(2, ep.params[1], 5e-2);
        }

    }


    @ParameterizedTest
    @MethodSource("parameterEstimators")
    void testLogNormal(IParameterEstimator estimator) {
        PgDistribution distribution = new PgLogNormalDistribution(2, 0.8);

        for (long seed = 0; seed < 10; seed++) {
            Random rand = new Random(seed);
            Sample sample = distribution.generate(15000, rand);

            EstimatedParameters ep = estimator.fit(sample, PgDistributionType.LOGNORMAL);

            Assertions.assertEquals(2, ep.params[0], 5e-2);
            Assertions.assertEquals(0.8, ep.params[1], 5e-2);
        }
    }

    @ParameterizedTest
    @MethodSource("parameterEstimators")
    void testWeibull(IParameterEstimator estimator) {
        PgDistribution distribution = new PgWeibullDistribution(2, 0.8);

        for (long seed = 0; seed < 10; seed++) {
            Random rand = new Random(seed);
            Sample sample = distribution.generate(10000, rand);

            EstimatedParameters ep = estimator.fit(sample, PgDistributionType.WEIBULL);

            Assertions.assertEquals(2, ep.params[0], 5e-2);
            Assertions.assertEquals(0.8, ep.params[1], 5e-2);
        }
    }

    @ParameterizedTest
    @MethodSource("parameterEstimators")
    void testGumbel(IParameterEstimator estimator) {
        PgDistribution distribution = new PgGumbelDistribution(2, 0.8);

        for (long seed = 0; seed < 10; seed++) {
            Random rand = new Random(seed);
            Sample sample = distribution.generate(10000, rand);

            EstimatedParameters ep = estimator.fit(sample, PgDistributionType.GUMBEL);

            Assertions.assertEquals(2, ep.params[0], 5e-2);
            Assertions.assertEquals(0.8, ep.params[1], 5e-2);
        }
    }
}
