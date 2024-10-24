package ru.postgrespro.perf.pgmicrobench.statanalyzer.criteria;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.util.Arrays;

public class Pearson {
	public static double statistic(int[] a, int[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException();
		}
		int sum = Arrays.stream(a).sum();

		double statistic = 0.0;

		for (int i = 0; i < a.length; i++) {
			statistic += ((double) a[i] * a[i]) / b[i];
		}

		return statistic - sum;
	}

	public static double pValue(int[] a, int[] b, int degreesOfFreedom) {
		double statistic = statistic(a, b);
		ChiSquaredDistribution d = new ChiSquaredDistribution(a.length - 1 - degreesOfFreedom);
		return 1 - d.cumulativeProbability(statistic);
	}


	public static void main(String[] args) {
		double pValue = pValue(new int[]{6, 13, 38, 74, 106, 85, 30, 14}, new int[]{3, 14, 42, 82, 99, 76, 37, 13}, 2);

		System.out.println("p-value: " + pValue);
	}

}
