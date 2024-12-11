package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Represents Gumbel distribution, probability distribution used to model
 * distribution of maximum (or minimum) of number of samples of various distributions.
 */

public class PgGumbelDistribution implements PgDistribution {
    private static final double EulerMascheroni = 0.57721566490153286060651209008240243104215933593992;
    private final double location;
    private final double scale;

    public PgGumbelDistribution() {
        this(0, 1);
    }

    public PgGumbelDistribution(double location) {
        this(location, 1);
    }

    /**
     * Gumbel distribution.
     */
    public PgGumbelDistribution(double location, double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }
        this.location = location;
        this.scale = scale;
    }

    /**
     * Generation.
     */
    public static Sample generate(Random random,
                                        double location,
                                        double scale,
                                        int count) {
        PgGumbelDistribution distribution = new PgGumbelDistribution(location, scale);
        return distribution.generate(count, random);
    }

    /**
     * PDF.
     */
    @Override
    public double pdf(double x) {
        double z = z(x);
        return Math.exp(-(z + Math.exp(-z))) / scale;
    }

    /**
     * CDF.
     */
    @Override
    public double cdf(double x) {
        return Math.exp(-Math.exp(-z(x)));
    }

    /**
     * Mean.
     */
    @Override
    public double mean() {
        return location + scale * EulerMascheroni;
    }

    /**
     * Median.
     */
    @Override
    public double median() {
        return location - scale * Math.log(Math.log(2));
    }

    /**
     * Variance.
     */
    @Override
    public double variance() {
        return (Math.PI * Math.PI * scale * scale) / 6;
    }


    @Override
    public double skewness() {
        return 1.139547099404;
    }


    @Override
    public double kurtosis() {
        return 5.4;
    }

    /**
     * Generation.
     */
    @Override
    public Sample generate(int count, Random random) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(random(random));
        }
        return new Sample(values);
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.GUMBEL;
    }

    /**
     * Quantile.
     */
    public double quantile(double p) {
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return location - scale * Math.log(-Math.log(p));
    }

    /**
     * Random.
     */
    public double random(Random random) {
        double u = random.nextDouble();
        return location - scale * Math.log(-Math.log(u));
    }


    /**
     * Standart deviation.
     */
    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    private double z(double x) {
        return (x - location) / scale;
    }
}
