package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

/**
 * The PgLogNormalDistribution class implements log-normal distribution.
 */
public class PgLogNormalDistribution implements PgSimpleDistribution {
    private final double mean;
    private final double standardDeviation;

    /**
     * Constructor.
     */
    public PgLogNormalDistribution(double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation must be positive");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    @Override
    public double pdf(double value) {
        if (value <= 0) {
            return 0;
        }
        double logValue = Math.log(value);
        return (1 / (value * standardDeviation * sqrt(2 * Math.PI)))
                * exp(-Math.pow(logValue - mean, 2) / (2 * standardDeviation * standardDeviation));
    }

    @Override
    public double cdf(double value) {
        if (value <= 0) {
            return 0;
        }
        double logValue = Math.log(value);
        return 0.5 * (1 + Erf.erf((logValue - mean) / (standardDeviation * sqrt(2))));
    }

    @Override
    public double mean() {
        return exp(mean + standardDeviation * standardDeviation / 2);
    }

    @Override
    public double variance() {
        return (exp(standardDeviation * standardDeviation) - 1) * exp(2 * mean + standardDeviation * standardDeviation);
    }

    @Override
    public double median() {
        return exp(mean);
    }


    @Override
    public double skewness() {
        return (exp(standardDeviation * standardDeviation) + 2) * sqrt(expm1(standardDeviation * standardDeviation));
    }


    @Override
    public double kurtosis() {
        return 3 * exp(2 * standardDeviation * standardDeviation)
                + 2 * exp(3 * standardDeviation * standardDeviation)
                + exp(4 * standardDeviation * standardDeviation) - 3;
    }


    @Override
    public Sample generate(int size, Random random) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double sample = exp(mean + standardDeviation * random.nextGaussian());
            values.add(sample);
        }
        return new Sample(values);
    }

    @Override
    public PgDistributionType getType() {
        return PgDistributionType.LOGNORMAL;
    }

    @Override
    public int getParamNumber() {
        return 2;
    }

    @Override
    public PgDistribution newDistribution(double[] params) {
        return new PgLogNormalDistribution(params[0], params[1]);
    }

    @Override
    public PgLogNormalDistribution newDistribution(Sample sample) {
        double meanSquare = sample.getMean() * sample.getMean();

        double stdDevSquare = FastMath.log((sample.getVariance() + meanSquare) / meanSquare);
        double mean = FastMath.log(sample.getMean()) - stdDevSquare / 2.0;

        return new PgLogNormalDistribution(mean, FastMath.sqrt(stdDevSquare));
    }

    @Override
    public double[] getParamArray() {
        return new double[]{mean, standardDeviation};
    }

    @Override
    public Pair<double[]> bounds() {
        return new Pair<>(new double[]{Double.NEGATIVE_INFINITY, 1e-6},
                new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY});
    }

    @Override
    public String toString() {
        return String.format("LogNormal(mean=%.3g, standardDeviation=%.3g)", mean, standardDeviation);
    }
}
