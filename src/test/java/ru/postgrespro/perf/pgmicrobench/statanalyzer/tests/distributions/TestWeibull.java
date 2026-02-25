package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgWeibullDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.Random;

public class TestWeibull {
    @Test
    public void testWeibull() {
        PgWeibullDistribution dist = new PgWeibullDistribution(7.0, 13.0);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);

        Assertions.assertEquals(7.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(13.0, newDist.getParamArray()[1], 0.5);
    }

    @Test
    public void testWeibull2() {
        PgWeibullDistribution dist = new PgWeibullDistribution(17.0, 3.0);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);

        Assertions.assertEquals(17.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(3.0, newDist.getParamArray()[1], 0.5);
    }
}