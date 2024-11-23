package ru.postgrespro.perf.pgmicrobench.statanalyzer.parsing;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import java.util.function.Function;

/**
 * The DistributionType enum defines various types of probability distributions
 * and provides methods to create instances of these distributions.
 */
public enum DistributionType {
    LOGNORM(2, (params -> new LogNormalDistribution(params[0], params[1]))),
    GAMMA(2, (params -> new GammaDistribution(params[0], params[1]))),
    ;

    private final int parameters;
    private final Function<double[], RealDistribution> createDistributionFunction;

    /**
     * Constructs a DistributionType with the specified number of parameters
     * and a function to create the corresponding RealDistribution.
     *
     * @param parameters                 the number of parameters required for the distribution.
     * @param createDistributionFunction a function that creates a RealDistribution
     *                                   from the given parameters.
     */
    DistributionType(int parameters, Function<double[], RealDistribution> createDistributionFunction) {
        this.parameters = parameters;
        this.createDistributionFunction = createDistributionFunction;
    }

    /**
     * Returns the number of parameters required for this distribution type.
     *
     * @return the number of parameters.
     */
    public int getParameters() {
        return parameters;
    }

    /**
     * Creates a RealDistribution instance using the provided parameters.
     *
     * @param params an array of double values representing the parameters for the distribution.
     * @return a RealDistribution instance created using the specified parameters.
     */
    public RealDistribution createDistribution(double[] params) {
        return createDistributionFunction.apply(params);
    }
}
