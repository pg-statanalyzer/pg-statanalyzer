package ru.postgrespro.perf.pgmicrobench.statanalyzer.tests.distributions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgFrechetDistribution;

import java.util.Random;

public class TestFrechet {
    @Test
    public void testFrechet() {
        PgFrechetDistribution dist = new PgFrechetDistribution(7.0, 5.0);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);

        System.out.println(newDist);
        Assertions.assertEquals(7.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(5.0, newDist.getParamArray()[1], 1.0);
    }

    @Test
    public void testFrechet2() {
        PgFrechetDistribution dist = new PgFrechetDistribution(13.0, 3.0);
        Sample s = dist.generate(3000, new Random(1));

        PgDistribution newDist = dist.newDistribution(s);

        System.out.println(newDist);
        Assertions.assertEquals(13.0, newDist.getParamArray()[0], 1.0);
        Assertions.assertEquals(3.0, newDist.getParamArray()[1], 0.5);
    }
}
