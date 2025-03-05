package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgNormalDistribution;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Applies jittering (random variation) to a list of values.
 */
public class Jittering {

    /**
     * Distribution used to generate noise.
     */
    private final PgDistribution distribution;

    /**
     * Constructs a Jittering instance with the provided noise distribution.
     *
     * @param simpleDistribution the noise distribution to be used.
     */
    public Jittering(PgDistribution simpleDistribution) {
        this.distribution = simpleDistribution;
    }

    /**
     * Constructs a Jittering instance with a default normal distribution
     * (mean=0, stddev=0.5).
     */
    public Jittering() {
        this.distribution = new PgNormalDistribution(0, 0.5);
    }

    /**
     * Applies jitter to each value in the list. Each value is modified as follows:
     * newValue = originalValue + noise * (originalValue / 100).
     *
     * @param values the list of original values.
     * @param random instance of Random used to generate noise.
     * @return a new list with jittered values.
     */
    public List<Double> jitter(List<Double> values, Random random) {
        Sample noise = distribution.generate(values.size(), random);

        return IntStream.range(0, values.size())
                .mapToObj(i -> {
                    double value = values.get(i);
                    return value + noise.getValues().get(i) * value / 100;
                })
                .collect(Collectors.toList());
    }
}