package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgLogNormalDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.Random;

public class TestLogNormal {
    @Test
    public void testLogNormal() {
        PgLogNormalDistribution dist = new PgLogNormalDistribution(13.0, 0.5);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);
        System.out.println(newDist);

        Assertions.assertEquals(13.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(0.5, newDist.getParamArray()[1], 0.1);
    }

    @Test
    public void testLogNormal2() {
        PgLogNormalDistribution dist = new PgLogNormalDistribution(7.0, 1.5);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);
        System.out.println(newDist);

        Assertions.assertEquals(7.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(1.5, newDist.getParamArray()[1], 0.1);
    }
}
