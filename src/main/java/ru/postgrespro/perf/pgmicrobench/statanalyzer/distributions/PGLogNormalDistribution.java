package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import static ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.AbstractDistribution.pearsonFitImplementation;

public class PGLogNormalDistribution {
	private static final int PARAMETER_NUMBER = 2;

	static RealDistribution pearsonFit(double[] data, double[] startPoint) {
		return pearsonFitImplementation(data, startPoint, PARAMETER_NUMBER, (params -> {
			if (params[1] <= 0) {
				throw new IllegalArgumentException("Wrong number of parameters");
			}
			return new LogNormalDistribution(params[0], params[1]);
		}));
	}


	public static void main(String[] args) {
		LogNormalDistribution nd = new LogNormalDistribution(1, 5);
		double[] sample = nd.sample(10000);

		pearsonFit(sample, new double[]{1, 1});
	}

}
