package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

public class PGNormalDistribution extends AbstractDistribution {
	private static final int PARAMETER_NUMBER = 2;

	static RealDistribution pearsonFit(double[] data, double[] startPoint) {
		return pearsonFitImplementation(data, startPoint, PARAMETER_NUMBER, (params -> {
			if (params[1] <= 0) {
				throw new IllegalArgumentException("Wrong number of parameters");
			}
			return new NormalDistribution(params[0], params[1]);
		}));
	}

	public static void main(String[] args) {
		NormalDistribution nd = new NormalDistribution(-10, 5);
		double[] sample = nd.sample(100000);

		pearsonFit(sample, new double[]{1, 1});
	}

	@Override
	double cdf(double x) {


		return 0;
	}

}
