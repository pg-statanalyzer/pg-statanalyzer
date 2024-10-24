package ru.postgrespro.perf.pgmicrobench.statanalyzer.distribution;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

public class FrechetDistribution implements RealDistribution {
	private final double alpha;
	private final double beta;
	private final double gamma;
	private final RandomGenerator random;

	public FrechetDistribution(double alpha, double beta, double gamma, RandomGenerator random) {
		if (alpha <= 0 || beta <= 0) {
			throw new IllegalArgumentException("Alpha and beta must be positive.");
		}
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.random = random;
	}

	@Override
	public double probability(double v) {
		return 0;
	}

	@Override
	public double density(double x) {
		if (x <= gamma) {
			return 0;
		}
		return (beta / alpha) * FastMath.pow((x - gamma) / alpha, beta - 1) * FastMath.exp(-FastMath.pow((x - gamma) / alpha, -beta));
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x <= gamma) {
			return 0;
		}
		return FastMath.exp(-FastMath.pow((x - gamma) / alpha, -beta));
	}

	@Override
	public double cumulativeProbability(double x0, double x1) throws NumberIsTooLargeException {
		if (x0 > x1) {
			throw new NumberIsTooLargeException(x0, x1, true);
		}
		return cumulativeProbability(x1) - cumulativeProbability(x0);
	}

	@Override
	public double inverseCumulativeProbability(double p) throws OutOfRangeException {
		if (p < 0 || p > 1) {
			throw new OutOfRangeException(p, 0, 1);
		}
		return gamma + alpha * FastMath.pow(-FastMath.log(p), -1 / beta);
	}

	@Override
	public double getNumericalMean() {
		if (beta <= 1) {
			return Double.NaN; // среднее не существует
		}
		return gamma + alpha * Gamma.gamma(1 - 1 / beta);
	}

	@Override
	public double getNumericalVariance() {
		if (beta <= 2) {
			return Double.NaN; // дисперсия не существует
		}
		double mean = getNumericalMean();
		return (alpha * alpha) * (Gamma.gamma(1 - 2 / beta) - FastMath.pow(mean - gamma, 2));
	}

	@Override
	public double getSupportLowerBound() {
		return gamma;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return false;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	@Override
	public boolean isSupportConnected() {
		return true;
	}

	@Override
	public void reseedRandomGenerator(long seed) {
		random.setSeed(seed);
	}

	@Override
	public double sample() {
		return inverseCumulativeProbability(random.nextDouble());
	}

	@Override
	public double[] sample(int size) {
		double[] samples = new double[size];
		for (int i = 0; i < size; i++) {
			samples[i] = sample();
		}
		return samples;
	}
}
