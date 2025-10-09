package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Gamma;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.util.PgMath;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PgFrechetDistribution implements PgSimpleDistribution {
    private static final double ZERO = 1e-6;

    private final double shape;
    private final double scale;
    private final double ratio;

    public PgFrechetDistribution(double shape, double scale) {
        if (shape < ZERO || scale < ZERO) {
            throw new IllegalArgumentException("Incorrect parameters");
        }

        this.shape = shape;
        this.scale = scale;
        this.ratio = shape / scale;
    }

    @Override
    public double mean() {
        if (shape <= 1) {
            throw new IllegalStateException("shape <= 1");
        }
        return scale * Gamma.gamma(1 - 1.0 / shape);
    }

    @Override
    public double variance() {
        if (shape <= 2) {
            throw new IllegalStateException("shape <= 2");
        }
        double m = Gamma.gamma(1 - 1.0 / shape);
        double m2 = Gamma.gamma(1 - 2.0 / shape);
        return scale * scale * (m2 - m * m);
    }

    @Override
    public double median() {
        return scale / Math.pow(Math.log(2), 1.0 / shape);
    }

    @Override
    public double skewness() {
        if (shape <= 3) {
            throw new IllegalStateException("shape <= 3");
        }
        double m = Gamma.gamma(1 - 1.0 / shape);
        double m2 = Gamma.gamma(1 - 2.0 / shape);
        double m3 = Gamma.gamma(1 - 3.0 / shape);
        double variance = scale * scale * (m2 - m * m);
        double sigma = Math.sqrt(variance);
        double thirdCentralMoment = scale * scale * scale * (m3 - 3 * m * m2 + 2 * m * m * m);
        return thirdCentralMoment / (sigma * sigma * sigma);
    }

    @Override
    public double kurtosis() {
        if (shape <= 4) {
            throw new IllegalStateException("shape <= 4");
        }
        double m = Gamma.gamma(1 - 1.0 / shape);
        double m2 = Gamma.gamma(1 - 2.0 / shape);
        double m3 = Gamma.gamma(1 - 3.0 / shape);
        double m4 = Gamma.gamma(1 - 4.0 / shape);
        double variance = scale * scale * (m2 - m * m);
        double fourthCentralMoment = scale * scale * scale * scale * (m4 - 4 * m * m3 + 6 * m * m * m2 - 3 * m * m * m * m);
        double kurtosis = fourthCentralMoment / (variance * variance);
        return kurtosis - 3;
    }

    @Override
    public double standardDeviation() {
        return PgSimpleDistribution.super.standardDeviation();
    }

    @Override
    public double pdf(double value) {
        double v = value / scale;
        double x = Math.pow(v, -shape);
        return ratio * (x / v) * Math.exp(-x);
    }

    @Override
    public double cdf(double value) {
        double v = value / scale;
        double x = Math.pow(v, -shape);
        return Math.exp(-x);
    }

    @Override
    public Sample generate(int size, Random random) {
        return new Sample(IntStream.range(0, size)
                .mapToObj(i -> {
                    double u = random.nextDouble();
                    return scale * Math.pow(Math.log(1 / u), -1.0 / shape);
                }).collect(Collectors.toList()));
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.FRECHET;
    }

    @Override
    public int getParamNumber() {
        return 2;
    }

    @Override
    public PgDistribution newDistribution(double[] params) {
        return new PgFrechetDistribution(params[0], params[1]);
    }

    @Override
    public PgFrechetDistribution newDistribution(Sample sample) {
        double meanSquare = sample.getMean() *  sample.getMean();

        double invAlpha = PgMath.minusInvSquareGammaDoubleGammaRatio(
                meanSquare / (meanSquare + sample.getVariance()));

        double alpha = 1 / invAlpha;
        double beta = sample.getMean() / Gamma.gamma(1 - invAlpha);

        return new PgFrechetDistribution(alpha, beta);
    }

    @Override
    public double[] getParamArray() {
        return new double[]{shape, scale};
    }

    @Override
    public Pair<double[]> bounds() {
        return new Pair<>(new double[]{ZERO, ZERO},
                new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY});
    }

    @Override
    public String toString() {
        return String.format("Frechet(shape=%.3g, scale=%.3g)", shape, scale);
    }
}
