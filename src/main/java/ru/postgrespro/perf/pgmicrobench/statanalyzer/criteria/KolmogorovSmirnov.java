package ru.postgrespro.perf.pgmicrobench.statanalyzer.criteria;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class KolmogorovSmirnov {
	public static void main(String[] args) {
		double[] sample = {1.2, 2.3, 2.1, 1.8, 2.5, 1.9, 2.0, 1.7};

		double mean = 2.0;
		double stdDev = 0.5;

		NormalDistribution normalDistribution = new NormalDistribution(mean, stdDev);

		KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();

		double statistic = ksTest.kolmogorovSmirnovStatistic(normalDistribution, sample);
		double pValue = ksTest.kolmogorovSmirnovTest(normalDistribution, sample);

		System.out.println("Statistic: " + statistic);
		System.out.println("p-value: " + pValue);
	}
}
