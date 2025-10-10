package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Gamma;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.util.PgMath;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.pow;

/**
 * The PgWeibullDistribution class implements weibull distribution.
 */
public class PgWeibullDistribution implements PgSimpleDistribution {
    private final double shape;
    private final double scale;

    /**
     * Constructor.
     */
    public PgWeibullDistribution(double shape, double scale) {
        this.shape = shape;
        this.scale = scale;
        if (shape <= 0) {
            throw new IllegalArgumentException("Shape must be positive");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }
    }

    @Override
    public double pdf(double value) {
        if (value < 0) {
            return 0;
        }
        return (shape / scale) * pow(value / scale, shape - 1) * Math.exp(-pow(value / scale, shape));
    }

    @Override
    public double cdf(double value) {
        if (value < 0) {
            return 0;
        }
        return 1 - Math.exp(-pow(value / scale, shape));
    }

    @Override
    public double mean() {
        return scale * Gamma.gamma(1 + 1 / shape);
    }

    @Override
    public double variance() {
        return scale * scale * (Gamma.gamma(1 + 2 / shape) - pow(Gamma.gamma(1 + 1 / shape), 2));
    }

    @Override
    public double median() {
        return scale * pow(Math.log(2), 1 / shape);
    }

    @Override
    public double skewness() {
        double mu = mean();
        double stnDev = Math.sqrt(variance());
        return (Gamma.gamma(1 + 3 / shape) * pow(scale, 3) - 3 * mu * pow(stnDev, 2) - pow(mu, 3))
                / pow(stnDev, 3);
    }

    @Override
    public double kurtosis() {
        double mu = mean();
        double stnDev = Math.sqrt(variance());
        return (pow(scale, 4) * Gamma.gamma(1 + 4 / shape)
                - 4 * skewness() * pow(stnDev, 3) * mu
                - 6 * pow(mu, 2) * pow(stnDev, 2)
                - pow(mu, 4)
        ) / pow(stnDev, 4);
    }

    @Override
    public Sample generate(int size, Random random) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double u = random.nextDouble();
            double sample = scale * pow(-Math.log(1 - u), 1 / shape);
            values.add(sample);
        }
        return new Sample(values);
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.WEIBULL;
    }

    @Override
    public int getParamNumber() {
        return 2;
    }

    @Override
    public PgDistribution newDistribution(double[] params) {
        return new PgWeibullDistribution(params[0], params[1]);
    }

    @Override
    public PgWeibullDistribution newDistribution(Sample sample) {
        double meanSquare = sample.getMean() *  sample.getMean();

        double invAlpha = PgMath.invSquareGammaDoubleGammaRatio(
                meanSquare / (meanSquare + sample.getVariance()));

        double alpha = 1 / invAlpha;
        double beta = sample.getMean() / Gamma.gamma(1 + invAlpha);

        return new PgWeibullDistribution(alpha, beta);
    }

    @Override
    public double[] getParamArray() {
        return new double[]{shape, scale};
    }

    @Override
    public Pair<double[]> bounds() {
        return new Pair<>(new double[]{1e-6, 1e-6},
                new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY});
    }

    @Override
    public String toString() {
        return "Weibull(shape=" + String.format("%.2f", shape) + ", scale=" + String.format("%.2f", scale) + ")";

    }
}
