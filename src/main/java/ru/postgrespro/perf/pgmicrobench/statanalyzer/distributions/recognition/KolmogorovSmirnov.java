package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionSample;

import java.util.Arrays;

public class KolmogorovSmirnov {
    private static final KolmogorovSmirnovTest KS_TEST = new KolmogorovSmirnovTest();


    public static double ksStatistic(double[] data, PgDistributionSample distribution) {
        int n = data.length;
        double nd = n;
        double[] dataCopy = new double[n];
        System.arraycopy(data, 0, dataCopy, 0, n);
        Arrays.sort(dataCopy);
        double d = 0.0;

        for (int i = 1; i <= n; ++i) {
            double yi = distribution.cdf(dataCopy[i - 1]);
            double currD = Math.max(i / nd - yi, yi - (i - 1) / nd);
            if (currD > d) {
                d = currD;
            }
        }

        return d;
    }

    public static double ksTest(double[] data, PgDistributionSample distribution) {
        return ksTest(ksStatistic(data, distribution), data.length);
    }

    public static double ksTest(double statistic, int n) {
        return 1.0 - KS_TEST.cdf(statistic, n);
    }

    public static FittedDistribution fit(double[] data, double[] startPoint, PgDistribution abstractDistribution) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistributionSample distribution;
            try {
                distribution = abstractDistribution.getSample(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            return ksStatistic(data, distribution);
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
        double statistic = result.getValue();
        double pValue = ksTest(statistic, data.length);

        return new FittedDistribution(solution, abstractDistribution.getSample(solution), pValue);
    }
}
