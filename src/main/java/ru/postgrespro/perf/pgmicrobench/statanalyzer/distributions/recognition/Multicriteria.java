package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


public class Multicriteria implements IDistributionTest, IParameterEstimator {

    /**
     * Computes the average deviation between the CDF of the dataset and the distribution.
     *
     * @param sample         the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return the average deviation in CDF.
     */
    private static double avgDeviationInCdf(Sample sample, PgDistribution pgDistribution) {
        List<Double> sortedValues = sample.getSortedValues();

        double sumOfDeltas = 0;
        for (int i = 0; i < sortedValues.size(); i++) {
            double sortedValue = sortedValues.get(i);
            sumOfDeltas += Math.abs(pgDistribution.cdf(sortedValue) - ((double) (i + 1) / sample.size()));
        }

        return sumOfDeltas / sample.size();
    }

    /**
     * Computes the average deviation between the PDF of the dataset and the distribution.
     *
     * @param sample         the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return the average deviation in PDF.
     */
    private static double avgDeviationInPdf(Sample sample, PgDistribution pgDistribution) {
        int bins = (int) sqrt(sample.size()) + 1;
        double min = sample.getMin();
        double max = sample.getMax();
        double binWidth = (max - min) / bins;

        List<Double> sortedValues = sample.getSortedValues();
        double sumOfDeltas = 0;
        double binBorder = min + binWidth;
        List<Double> part = new LinkedList<>();
        for (int i = 0; i < sample.size(); ) {
            double value = sortedValues.get(i);
            if (value <= binBorder) {
                part.add(value);
                i++;
            } else {
                double pdf = (double) part.size() / sample.size() / binWidth;
                for (double datum : part) {
                    sumOfDeltas += Math.abs(pgDistribution.pdf(datum) - pdf);
                }
                part.clear();
                binBorder += binWidth;
            }
        }

        double pdf = (double) part.size() / sample.size() / binWidth;
        for (double datum : part) {
            sumOfDeltas += Math.abs(pgDistribution.pdf(datum) - pdf);
        }

        return sumOfDeltas / sample.size();
    }

    /**
     * Calculates the deviation between the skewness and kurtosis of the dataset and the distribution.
     *
     * @param sample         the input dataset statistic.
     * @param pgDistribution the distribution to compare against.
     * @return the combined deviation in skewness and kurtosis.
     */
    private static double deviationInSkewAndKurt(Sample sample, PgDistribution pgDistribution) {
        double kurt2 = sample.getKurtosis();
        double skew2 = sample.getSkewness();

        double kurt1 = pgDistribution.kurtosis();
        double skew1 = pgDistribution.skewness();

        return sqrt(pow((skew1 - skew2), 2) + pow((kurt1 - kurt2), 2));
    }

    /**
     * Calculates a p-value based on the given multicriteria statistic.
     *
     * @param sample input data.
     * @return the computed p-value.
     */
    @Override
    public double test(Sample sample, PgDistribution distribution) {
        return 1 - statistic(sample, distribution);
    }

    /**
     * Computes a multicriteria statistic for evaluating the goodness-of-fit of a distribution.
     * The statistic incorporates measures such as CDF and PDF deviations, Kolmogorov-Smirnov
     * statistic, skewness, and kurtosis.
     *
     * @param sample       the input dataset.
     * @param distribution the distribution to compare against.
     * @return a combined multicriteria statistic.
     */
    public double statistic(Sample sample, PgDistribution distribution) {
        return avgDeviationInCdf(sample, distribution)
                * avgDeviationInPdf(sample, distribution)
                * CramerVonMises.cvmStatistic(sample, distribution)
                * deviationInSkewAndKurt(sample, distribution);
    }

    /**
     * Fits a statistical distribution to the given data using a multicriteria optimization approach.
     *
     * @param sample the input dataset to fit the distribution to.
     * @param type   the type of the distribution to fit
     * @return a {@link FittedDistribution} object with fitted parameters, sample, and p-value
     */
    @Override
    public EstimatedParameters fit(Sample sample, PgDistributionType type) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = type.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            return statistic(sample, distribution);
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(evaluationFunction),
                GoalType.MINIMIZE,
                new InitialGuess(type.getStartPoint()),
                new NelderMeadSimplex(type.getParameterNumber())
        );

        double[] solution = result.getPoint();
        double pValue = 1 - result.getValue();

        return new EstimatedParameters(solution, type.createDistribution(solution), pValue);
    }
}
