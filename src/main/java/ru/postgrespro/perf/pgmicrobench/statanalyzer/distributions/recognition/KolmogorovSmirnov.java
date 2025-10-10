package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer.PgOptimizer;

import java.util.List;

/**
 * This class provides methods to perform the Kolmogorov-Smirnov test for goodness of fit
 * and to fit a distribution to a given dataset using the Kolmogorov-Smirnov statistic.
 */
public class KolmogorovSmirnov implements IDistributionTest, IParameterEstimator {
    private static final KolmogorovSmirnovTest KS_TEST = new KolmogorovSmirnovTest();

    /**
     * Calculates the Kolmogorov-Smirnov statistic for the given data and distribution.
     *
     * @param sample       the observed data
     * @param distribution the theoretical distribution to compare against
     * @return the Kolmogorov-Smirnov statistic
     */
    @Override
    public double statistic(Sample sample, PgDistribution distribution) {
        double nd = sample.size();
        List<Double> sortedValues = sample.getSortedValues();
        double d = 0.0;

        for (int i = 1; i <= sample.size(); ++i) {
            double yi = distribution.cdf(sortedValues.get(i - 1));
            double currD = Math.max(i / nd - yi, yi - (i - 1) / nd);
            if (currD > d) {
                d = currD;
            }
        }

        return d;
    }

    /**
     * Performs the Kolmogorov-Smirnov test on the given data against a specified distribution.
     *
     * @param sample       the observed data
     * @param distribution the theoretical distribution to compare against
     * @return the p-value of the Kolmogorov-Smirnov test
     */
    @Override
    public double test(Sample sample, PgDistribution distribution) {
        return test(statistic(sample, distribution), sample.size());
    }

    /**
     * Calculates the p-value for a given Kolmogorov-Smirnov statistic and sample size.
     *
     * @param statistic the Kolmogorov-Smirnov statistic
     * @param n         the sample size
     * @return the p-value corresponding to the statistic
     */
    public double test(double statistic, int n) {
        return 1.0 - KS_TEST.cdf(statistic, n);
    }

    /**
     * Fits a distribution to the observed data by minimizing the Kolmogorov-Smirnov statistic.
     *
     * @param sample       the observed data
     * @param distribution the type of distribution to fit
     * @return a EstimatedParameters object with fitted parameters, sample, and p-value
     */
    @Override
    public EstimatedParameters fit(Sample sample, PgSimpleDistribution distribution) {
        double[] solution = PgOptimizer.optimize(sample, distribution, new KolmogorovSmirnov());

        PgDistribution optimizedDist = distribution.newDistribution(solution);
        double pValue = test(sample, optimizedDist);

        return new EstimatedParameters(optimizedDist, pValue);
    }

    @Override
    public EstimatedParameters fit(Sample sample, PgCompositeDistribution distribution) {
        double[] solution = PgOptimizer.optimize(sample, distribution, new KolmogorovSmirnov());

        PgCompositeDistribution optimizedDist = distribution.newDistribution(solution);
        double pValue = test(sample, optimizedDist);

        return new EstimatedParameters(optimizedDist, pValue);
    }
}
