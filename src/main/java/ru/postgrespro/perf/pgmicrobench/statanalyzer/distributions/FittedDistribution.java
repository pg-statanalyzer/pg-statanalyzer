package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * Represents a fitted distribution along with its parameters and p-value.
 */
// TODO data class
public class FittedDistribution {
	public final double[] params;
	private final RealDistribution distribution;
	private final double pValue;

	/**
	 * Constructs a FittedDistribution object with the specified distribution, parameters, and p-value.
	 *
	 * @param distribution the distribution that has been fitted to the data
	 * @param params       the parameters of the fitted distribution
	 * @param pValue       the p-value indicating the goodness of fit of the distribution
	 */
	public FittedDistribution(RealDistribution distribution, double[] params, double pValue) {
		this.distribution = distribution;
		this.params = params;
		this.pValue = pValue;
	}

	/**
	 * @return the fitted distribution
	 */
	public RealDistribution getDistribution() {
		return distribution;
	}

	/**
	 * @return an array of parameters for the fitted distribution
	 */
	public double[] getParams() {
		return params;
	}

	/**
	 * @return the p-value indicating the fit quality
	 */
	public double getPValue() {
		return pValue;
	}
}
