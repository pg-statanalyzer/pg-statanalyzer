package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PgGammaDistribution represents Gamma distribution with shape (k) and scale (theta) parameters.
 */
public class PgGammaDistribution implements PgSimpleDistribution {
    private final double shape;
    private final double scale;

    /**
     * Default constructor.
     */
    public PgGammaDistribution() {
        this(1.0, 1.0);
    }

    /**
     * Constructor to create Gamma distribution with specified shape parameter,
     * scale parameter is set to 1.0 by default.
     *
     * @param shape shape parameter (must be positive)
     * @throws IllegalArgumentException if shape parameter is less than or equal to 0
     */
    public PgGammaDistribution(double shape) {
        this(shape, 1.0);
    }

    /**
     * Constructor to create Gamma distribution with specified shape and scale parameters.
     *
     * @param shape shape parameter (must be positive).
     * @param scale scale parameter (must be positive).
     * @throws IllegalArgumentException if either shape or scale parameters are less than or equal to 0.
     */
    public PgGammaDistribution(double shape, double scale) {
        if (shape <= 0 || scale <= 0) {
            throw new IllegalArgumentException("Shape and scale parameters must be positive");
        }
        this.shape = shape;
        this.scale = scale;
    }

    /**
     * PDF of Gamma distribution.
     */
    @Override
    public double pdf(double x) {
        if (x <= 0) {
            return 0.0;
        }
        double z = x / scale;
        return (Math.pow(z, shape - 1) * Math.exp(-z)) / (scale * gamma(shape));
    }

    /**
     * CDF approximation of Gamma distribution (using regularized incomplete gamma function).
     */
    @Override
    public double cdf(double x) {
        if (x <= 0) {
            return 0.0;
        }
        return regularizedGammaP(shape, x / scale);
    }

    /**
     * Mean of Gamma distribution.
     * E[X] = k * θ
     */
    @Override
    public double mean() {
        return shape * scale;
    }

    /**
     * Median of Gamma distribution (approximation).
     */
    @Override
    public double median() {
        return shape < 1 ? mean() : scale * (shape - 1.0 / 3.0 + 0.02 / shape);
    }

    /**
     * Variance of Gamma distribution.
     * Var[X] = k * θ^2
     */
    @Override
    public double variance() {
        return shape * scale * scale;
    }

    /**
     * Skewness of Gamma distribution.
     */
    @Override
    public double skewness() {
        return 1.139547099404;
    }

    /**
     * Kurtosis of Gamma distribution.
     */
    @Override
    public double kurtosis() {
        return 5.4;
    }

    /**
     * Standard deviation.
     */
    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    /**
     * Generates sample of Gamma distributed values.
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
        return null;
    }

    @Override
    public int getParamNumber() {
        return 2;
    }

    @Override
    public PgDistribution newDistribution(double[] params) {
        return new PgGammaDistribution(params[0], params[1]);
    }

    @Override
    public double[] getParamArray() {
        return new double[]{shape, scale};
    }

    @Override
    public Pair<double[]> bounds() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Quantile function (inverse CDF) approximation for Gamma distribution.
     */
    public double quantile(double p) {
        if (p <= 0) {
            return 0.0;
        }
        if (p >= 1) {
            return Double.POSITIVE_INFINITY;
        }
        return gammaQuantile(shape, scale, p);
    }

    /**
     * Generates single random value following Gamma distribution.
     */
    public double random(Random random) {
        if (shape < 1) {
            return random(random) * Math.pow(random.nextDouble(), 1.0 / shape);
        }
        double d = shape - 1.0 / 3.0;
        double c = 1.0 / Math.sqrt(9.0 * d);

        while (true) {
            double z;
            do {
                z = random.nextGaussian();
            } while (z <= -1 / c);

            double v = Math.pow(1 + c * z, 3);
            double u = random.nextDouble();

            if (u < 1 - 0.0331 * z * z * z * z || Math.log(u) < 0.5 * z * z + d * (1 - v + Math.log(v))) {
                return scale * d * v;
            }
        }
    }

    private double gamma(double x) {
        double[] p = {676.5203681218851, -1259.1392167224028, 771.32342877765313, -176.61502916214059,
                12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7
        };

        double g = 7.0;
        if (x < 0.5) {
            return Math.PI / (Math.sin(Math.PI * x) * gamma(1 - x));
        }

        x -= 1;
        double a = 0.99999999999980993;
        for (int i = 0; i < p.length; i++) {
            a += p[i] / (x + i + 1);
        }
        double t = x + g + 0.5;
        return Math.sqrt(2 * Math.PI) * Math.pow(t, x + 0.5) * Math.exp(-t) * a;
    }

    private double regularizedGammaP(double a, double x) {
        double sum = 0;
        double term = 1.0 / a;
        double ap = a;

        for (int n = 1; n < 100; n++) {
            ap += 1;
            term *= x / ap;
            sum += term;
            if (term < 1e-10) {
                break;
            }
        }

        return sum * Math.exp(-x + a * Math.log(x) - Math.log(gamma(a)));
    }

    private double gammaQuantile(double shape, double scale, double p) {
        double guess = mean();
        double error;
        do {
            error = cdf(guess) - p;
            guess -= error / pdf(guess);
        } while (Math.abs(error) > 1e-5);
        return guess;
    }
}
