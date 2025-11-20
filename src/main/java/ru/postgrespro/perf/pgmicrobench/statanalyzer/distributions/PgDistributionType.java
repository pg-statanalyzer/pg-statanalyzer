package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * enum for types of supported distributions
 * List of supported distributions:
 * Normal, LogNormal, Gumbel, Frechet, Weibull, Gamma
 */
@RequiredArgsConstructor
public enum PgDistributionType {
    NORMAL(2, params -> new PgNormalDistribution(params[0], params[1]), new double[]{1, 1}),
    LOGNORMAL(2, params -> new PgLogNormalDistribution(params[0], params[1]), new double[]{1, 0.5}),
    GUMBEL(2, params -> new PgGumbelDistribution(params[0], params[1]), new double[]{1, 1}),
    FRECHET(2, params -> new PgFrechetDistribution(params[0], params[1]), new double[]{2, 1}),
    //    UNIFORM(2, params -> new PgUniformDistribution(params[0], params[1]), new double[] {0, 1}),
    WEIBULL(2, params -> new PgWeibullDistribution(params[0], params[1]), new double[]{1, 1}),
    GAMMA(2, params -> new PgGammaDistribution(params[0], params[1]), new double[] {1, 1});

    @Getter
    private final int parameterNumber;
    private final Function<double[], PgDistribution> createDistributionFunction;
    @Getter
    private final double[] startPoint;

    public PgDistribution createDistribution(double[] params) {
        if (params.length != parameterNumber) {
            throw new IllegalArgumentException("Wrong number of parameters");
        }
        return createDistributionFunction.apply(params);
    }
}
