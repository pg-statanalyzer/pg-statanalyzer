package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Represents Gumbel distribution, probability distribution used to model
 * distribution of maximum (or minimum) of number of samples of various distributions.
 */

public class GumbelDistribution {

    private final double location;
    private final double scale;
    private static final double EulerMascheroni = 0.57721566490153286060651209008240243104215933593992;

    public GumbelDistribution() {
        this(0, 1);
    }

    public GumbelDistribution(double location) {
        this(location, 1);
    }

    public GumbelDistribution(double location, double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }
        this.location = location;
        this.scale = scale;
    }

    public double pdf(double x) {
        double z = z(x);
        return Math.exp(-(z + Math.exp(-z))) / scale;
    }

    public double cdf(double x) {
        return Math.exp(-Math.exp(-z(x)));
    }

    public double quantile(double p) {
        if (p == 0) return Double.NEGATIVE_INFINITY;
        if (p == 1) return Double.POSITIVE_INFINITY;
        return location - scale * Math.log(-Math.log(p));
    }

    public double random(Random random) {
        double u = random.nextDouble();
        return location - scale * Math.log(-Math.log(u));
    }

    public List<Double> generate(Random random, int count) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(random(random));
        }
        return values;
    }

    public static List<Double> generate(Random random,
                                        double location,
                                        double scale,
                                        int count) {
        GumbelDistribution distribution = new GumbelDistribution(location, scale);
        return distribution.generate(random, count);
    }

    public double mean() {
        return location + scale * EulerMascheroni;
    }

    public double median() {
        return location - scale * Math.log(Math.log(2));
    }

    public double variance() {
        return (Math.PI * Math.PI * scale * scale) / 6;
    }

    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    private double z(double x) {
        return (x - location) / scale;
    }
}
