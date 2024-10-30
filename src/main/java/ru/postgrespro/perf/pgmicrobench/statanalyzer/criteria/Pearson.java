package ru.postgrespro.perf.pgmicrobench.statanalyzer.criteria;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

import java.util.Arrays;

public class Pearson {
	public static double chiSquareTest(double[] arr, RealDistribution d, int degreesOfFreedom, int bins) {
		double p = 1. / bins;
		double pSquare = p * p;
		int N = arr.length;

		double[] bounds = boundsOfBins(arr, bins);

		double[] tP = new double[bins];
		double prev = 0;
		for (int i = 0; i < bins - 1; i++) {
			double cur = d.cumulativeProbability(bounds[i]);
			tP[i] = cur - prev;
			prev = cur;
		}
		tP[bins - 1] = 1 - prev;


		double statistic = 0;
		for (int i = 0; i < bins; i++) {
			statistic += pSquare / tP[i];
		}
		statistic -= 1;
		statistic *= N;


		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(bins - 1 - degreesOfFreedom);
		return 1 - chiSquaredDistribution.cumulativeProbability(statistic);
	}


	private static double[] boundsOfBins(double[] arr, int bins) {
		int N = arr.length;

		double[] sorted = Arrays.stream(arr).sorted().toArray();

		double[] bounds = new double[bins - 1];

		int amount = N / bins;
		int rem = N - amount * bins;
		int curPos = 0;

		for (int i = 0; i < bounds.length; i++) {
			curPos += amount + (i < rem ? 1 : 0);
			bounds[i] = sorted[curPos - 1];
		}

		return bounds;
	}
}
