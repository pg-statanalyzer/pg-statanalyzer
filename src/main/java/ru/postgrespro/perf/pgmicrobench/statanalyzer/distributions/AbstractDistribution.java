package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import java.util.Arrays;
import java.util.function.Function;

public abstract class AbstractDistribution {
	static RealDistribution pearsonFitImplementation(double[] data, double[] startPoint,
	                                                 int degreeOfFreedom, Function<double[], RealDistribution> getDistribution) {
		int bins = (int) Math.sqrt(data.length) + 1;

		double[] bounds = boundsOfBins(data, bins);

		MultivariateFunction evaluationFunction = point -> {
			RealDistribution distribution;
			try {
				distribution = getDistribution.apply(point);
			} catch (Exception e) {
				return Double.POSITIVE_INFINITY;
			}

			double[] tP = new double[bins];
			double prev = 0;
			for (int i = 0; i < bins - 1; i++) {
				double cur = distribution.cumulativeProbability(bounds[i]);
				tP[i] = cur - prev;
				prev = cur;
			}
			tP[bins - 1] = 1 - prev;

			double sum = 0;
			for (int i = 0; i < bins; i++) {
				sum += 1.0 / tP[i];
			}

			return sum;
		};

		SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
		PointValuePair result = optimizer.optimize(
				new MaxEval(10000),
				new ObjectiveFunction(evaluationFunction),
				GoalType.MINIMIZE,
				new InitialGuess(startPoint),
				new NelderMeadSimplex(2)
		);

		double[] solution = result.getPoint();
		double functionValue = result.getValue();

		double statistic = functionValue - bins * bins;
		statistic /= (bins * bins);
		statistic *= data.length;

		System.out.println("Оптимальное решение: x = " + solution[0] + ", y = " + solution[1]);
		System.out.println("Значение функции в минимуме: " + functionValue);


		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(bins - 1 - degreeOfFreedom);
		System.out.println("pValue " + (1 - chiSquaredDistribution.cumulativeProbability(statistic)));

		return getDistribution.apply(solution);
	}


	protected static double[] boundsOfBins(double[] arr, int bins) {
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

	abstract double cdf(double x);

}
