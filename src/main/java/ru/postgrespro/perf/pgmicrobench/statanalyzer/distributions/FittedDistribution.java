package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.distribution.RealDistribution;

public class FittedDistribution {
	private final RealDistribution distribution;
	public final double[] params;
	private final double pValue;

	public FittedDistribution(RealDistribution distribution, double[] params, double pValue) {
		this.distribution = distribution;
		this.params = params;
		this.pValue = pValue;
	}

	public RealDistribution getDistribution() {
		return distribution;
	}

	public double[] getParams() {
		return params;
	}

	public double getPValue() {
		return pValue;
	}
}
