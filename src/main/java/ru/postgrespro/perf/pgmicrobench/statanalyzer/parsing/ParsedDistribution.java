package ru.postgrespro.perf.pgmicrobench.statanalyzer.parsing;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * The ParsedDistribution class represents a collection of probability distributions
 * along with associated data. It provides access to the distributions and the data
 * used in the analysis.
 */
public class ParsedDistribution {
    private final RealDistribution[] distributions;
    private final double[] data;

    /**
     * Constructs a ParsedDistribution with the specified distributions and data.
     *
     * @param distributions an array of RealDistribution objects representing the distributions.
     * @param data          an array of double values representing the associated data.
     */
    ParsedDistribution(RealDistribution[] distributions, double[] data) {
        this.distributions = distributions;
        this.data = data;
    }

    /**
     * Returns the array of distributions contained in this ParsedDistribution.
     *
     * @return an array of RealDistribution objects.
     */
    public RealDistribution[] getDistribution() {
        return distributions;
    }

    /**
     * Returns the array of data associated with the distributions.
     *
     * @return an array of double values representing the data.
     */
    public double[] getData() {
        return data;
    }
}
