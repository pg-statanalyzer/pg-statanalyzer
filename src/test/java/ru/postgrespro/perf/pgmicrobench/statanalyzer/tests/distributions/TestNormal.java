package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgNormalDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.Random;

public class TestNormal {
    @Test
    public void testNormal() {
        PgNormalDistribution dist = new PgNormalDistribution(13.0, 3.0);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);

        Assertions.assertEquals(13.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(3.0, newDist.getParamArray()[1], 0.5);
    }
}
