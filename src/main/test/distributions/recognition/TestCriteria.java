package distributions.recognition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.CramerVonMises;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IDistributionTest;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Multicriteria;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class TestCriteria {
    static Stream<Arguments> distributionTests() {
        return Stream.of(
                Arguments.of(new CramerVonMises()),
                Arguments.of(new KolmogorovSmirnov()),
                Arguments.of(new Multicriteria())
        );
    }

    @ParameterizedTest
    @MethodSource("distributionTests")
    void testCriteria(IDistributionTest distributionTest) {
        List<PgDistribution> distributions = Arrays.asList(
                new PgNormalDistribution(1, 1),
                new PgLogNormalDistribution(1, 0.5),
                new PgGumbelDistribution(1, 1),
                new PgWeibullDistribution(1, 1)
        );


        for (long seed = 0;  seed < 20; seed++) {
            Random rand = new Random(seed);
            for (PgDistribution distribution : distributions) {
                Sample sample = distribution.generate(5000, rand);

                double pValue = distributionTest.test(sample, distribution);

                for (PgDistribution sd : distributions) {
                    double pValueSd = distributionTest.test(sample, sd);

                    Assertions.assertTrue(pValue >= pValueSd);
                }
            }
        }
    }
}
