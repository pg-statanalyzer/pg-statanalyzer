package util.parsing;

import org.apache.commons.math3.distribution.RealDistribution;

public class ParsedDistribution {
	private final RealDistribution[] distributions;
	private final double[] data;

	ParsedDistribution(RealDistribution[] distributions, double[] data) {
		this.distributions = distributions;
		this.data = data;
	}

	public RealDistribution[] getDistribution() {
		return distributions;
	}

	public double[] getData() {
		return data;
	}
}
