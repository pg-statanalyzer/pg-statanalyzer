package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public enum PgDistributionType {
    NORMAL(2, params -> new PgNormalDistribution(params[0], params[1])),
    LOGNORMAL(2, params -> new PgLogNormalDistribution(params[0], params[1])),
    GUMBEL(2, params -> new PgGumbelDistribution(params[0], params[1])),
    UNIFORM(2, params -> new PgUniformDistribution(params[0], params[1])),
    WEIBULL(2, params -> new PgWeibullDistribution(params[0], params[1]));

    @Getter
    private final int parameterNumber;
    private final Function<double[], PgDistribution> createDistributionFunction;

    public PgDistribution createDistribution(double[] params) {
        if (params.length != parameterNumber) {
            throw new IllegalArgumentException("Wrong number of parameters");
        }
        return createDistributionFunction.apply(params);
    }
}
