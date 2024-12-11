package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;

import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


public class Multicriteria implements IDistributionTest, IParameterEstimator {
    private static final IDistributionTest TEST = new CramerVonMises();

    /**
     * Calculates a p-value based on the given multicriteria statistic.
     *
     * @param statistic the multicriteria statistic value.
     * @return the computed p-value.
     */
    public static double multiCriteriaTest(double statistic) {
        return 1 - statistic;
    }

    /**
     * Computes the average deviation between the CDF of the dataset and the distribution.
     *
     * @param sample         the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return the average deviation in CDF.
     */
    private static double avgDeviationInCdf(Sample sample, PgDistribution pgDistribution) {
        double sumOfDeltas = 0;
        for (Double sortedValue : sample.getSortedValues()) {
            sumOfDeltas += Math.abs(pgDistribution.cdf(sortedValue) - cdfOfDataset(sortedValue, sample));
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
        double sumOfDeltas = 0;
        for (double datum : sample.getValues()) {
            sumOfDeltas += Math.abs(pgDistribution.pdf(datum) - pdfOfDataset(datum, sample));
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
    private static double deviationInSkewAndKurt(Sample sample , PgDistribution pgDistribution) {
        double kurt2 = sample.getKurtosis();
        double skew2 = sample.getSkewness();

        double kurt1 = pgDistribution.kurtosis();
        double skew1 = pgDistribution.skewness();

        return sqrt(pow((skew1 - skew2), 2) + pow((kurt1 - kurt2), 2));
    }

    /**
     * Computes the cumulative distribution function (CDF) of the dataset at a given point.
     *
     * @param x      the point at which to evaluate the CDF.
     * @param sample the dataset.
     * @return the CDF value at the specified point.
     */
    private static double cdfOfDataset(double x, Sample sample) {
        List<Double> sortedValues = sample.getSortedValues();

        int count = 0;
        for (Double sortedValue : sortedValues) {
            if (sortedValue <= x) {
                count++;
            }
        }

        return (double) count / sortedValues.size();
    }

    /**
     * Computes the probability density function (PDF) of the dataset at a given point.
     * The PDF is estimated using histogram binning.
     *
     * @param x    the point at which to evaluate the PDF.
     * @param sample the dataset.
     * @return the PDF value at the specified point.
     */
    private static double pdfOfDataset(double x, Sample sample) {
        int bins = 50;
        double min = sample.getMin();
        double max = sample.getMax();
        double binWidth = (max - min) / bins;

        int binIndex = (int) ((x - min) / binWidth);
        if (binIndex < 0 || binIndex >= bins) {
            return 0.0;
        }

        int countInBin = 0;
        for (double value : sample.getSortedValues()) {
            int currentBin = (int) ((value - min) / binWidth);
            if (currentBin == binIndex) {
                countInBin++;
            }
        }

        return (double) countInBin / (sample.size() * binWidth);
    }

    /**
     * Computes a multicriteria statistic for evaluating the goodness-of-fit of a distribution.
     * The statistic incorporates measures such as CDF and PDF deviations, Kolmogorov-Smirnov
     * statistic, skewness, and kurtosis.
     *
     * @param sample         the input dataset.
     * @param distribution the distribution to compare against.
     * @return a combined multicriteria statistic.
     */
    @Override
    public double test(Sample sample, PgDistribution distribution) {
        return
                avgDeviationInCdf(sample, distribution)
                        * avgDeviationInPdf(sample, distribution)
                        * TEST.test(sample, distribution)
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

            return test(sample, distribution);
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
        double statistic = result.getValue();
        double pValue = multiCriteriaTest(statistic);

        return new EstimatedParameters(solution, type.createDistribution(solution), pValue);
    }
}
