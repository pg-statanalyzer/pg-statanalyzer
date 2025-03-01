package ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.Well512a;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgSimpleDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IStatisticEvaluator;

public class PgOptimizer {
	private static final MaxEval MAX_EVAL = new MaxEval(10000);
	private static final CMAESOptimizer.PopulationSize POPULATION_SIZE = new CMAESOptimizer.PopulationSize(100);
	private static final double WEIGHT_STEP = 0.1;


	public static double[] optimize(Sample sample, PgCompositeDistribution distribution, IStatisticEvaluator statisticEvaluator) {
		final CMAESOptimizer optimizer = new CMAESOptimizer(
				10000,
				1e-4,
				true,
				1,
				5,
				new Well512a(42),
				false,
				null);
		MultivariateFunction evaluationFunction = point -> {
			PgCompositeDistribution d = distribution.newDistribution(point);

			double weightSum = 0;
			for (int i = 0; i < distribution.getSize(); i++) {
				weightSum += point[point.length - i - 1];
			}

			return statisticEvaluator.statistic(sample, d) + Math.pow(weightSum - 1, 2);
		};

		double[] startParam = distribution.getParamArray();
		double[] sigma = new double[distribution.getParamNumber()];
		int i = 0;
		for (; i < sigma.length - distribution.getSize(); i++) {
			sigma[i] = 0.3;
		}
		for (; i < sigma.length; i++) {
			sigma[i] = WEIGHT_STEP;
		}
		Pair<double[]> bounds = distribution.bounds();

		PointValuePair result = optimizer.optimize(
				MAX_EVAL,
				POPULATION_SIZE,
				GoalType.MINIMIZE,
				new ObjectiveFunction(evaluationFunction),
				new InitialGuess(startParam),
				new CMAESOptimizer.Sigma(sigma),
				new SimpleBounds(bounds.first, bounds.second));

		return result.getPoint();
	}

	public static double[] optimize(Sample sample, PgSimpleDistribution distribution, IStatisticEvaluator statisticEvaluator) {
		final CMAESOptimizer optimizer = new CMAESOptimizer(
				10000,
				1e-4,
				true,
				1,
				5,
				new Well512a(42),
				false,
				null);
		MultivariateFunction evaluationFunction = point -> {
			PgDistribution d = distribution.newDistribution(point);
			return statisticEvaluator.statistic(sample, d);
		};

		double[] startParam = distribution.getParamArray();
		double[] sigma = new double[distribution.getParamNumber()];
		for (int i = 0; i < sigma.length; i++) {
			sigma[i] = 1;
		}
		Pair<double[]> bounds = distribution.bounds();

		PointValuePair result = optimizer.optimize(
				MAX_EVAL,
				POPULATION_SIZE,
				GoalType.MINIMIZE,
				new ObjectiveFunction(evaluationFunction),
				new InitialGuess(startParam),
				new CMAESOptimizer.Sigma(sigma),
				new SimpleBounds(bounds.first, bounds.second)
		);

		return result.getPoint();
	}
}
