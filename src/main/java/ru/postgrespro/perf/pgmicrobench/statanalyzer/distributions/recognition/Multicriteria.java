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
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov.ksStatistic;


public class Multicriteria {
    private static final KolmogorovSmirnovTest KS_TEST = new KolmogorovSmirnovTest();

    /**
     * Fits a statistical distribution to the given data using a multicriteria optimization approach.
     *
     * @param data       the input dataset to fit the distribution to.
     * @param pgDistType the type of the distribution to fit
     * @return a {@link FittedDistribution} object with fitted parameters, sample, and p-value
     */
    public static FittedDistribution fit(double[] data, PgDistributionType pgDistType) {
        MultivariateFunction evaluationFunction = point -> {
            PgDistribution distribution;
            try {
                distribution = pgDistType.createDistribution(point);
            } catch (Exception e) {
                return Double.POSITIVE_INFINITY;
            }

            return multiCriteriaStatistic(data, distribution);
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(evaluationFunction),
                GoalType.MINIMIZE,
                new InitialGuess(pgDistType.getStartPoint()),
                new NelderMeadSimplex(pgDistType.getParameterNumber())
        );

        double[] solution = result.getPoint();
        double statistic = result.getValue();
        double pValue = multiCriteriaTest(statistic);

        return new FittedDistribution(solution, pgDistType.createDistribution(solution), pValue);
    }

    /**
     * Computes a multicriteria statistic for evaluating the goodness-of-fit of a distribution.
     * The statistic incorporates measures such as CDF and PDF deviations, Kolmogorov-Smirnov
     * statistic, skewness, and kurtosis.
     *
     * @param data          the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return a combined multicriteria statistic.
     */
    public static double multiCriteriaStatistic(double[] data, PgDistribution pgDistribution) {
        double[] dataCopy = new double[data.length];
        System.arraycopy(data, 0, dataCopy, 0, data.length);
        Arrays.sort(dataCopy);

        double ksStat = ksStatistic(dataCopy, pgDistribution);


        return
                avgDeviationInCdf(dataCopy, pgDistribution)
                * avgDeviationInPdf(dataCopy, pgDistribution)
                * KS_TEST.cdf(ksStat, data.length)
                * deviationInSkewAndKurt(dataCopy, pgDistribution);
    }

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
     * @param data          the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return the average deviation in CDF.
     */
    private static double avgDeviationInCdf(double[] data, PgDistribution pgDistribution) {
        double sumOfDeltas = 0;
        for (int i = 0; i < data.length; i++) {
            sumOfDeltas += Math.abs(pgDistribution.cdf(data[i]) - cdfOfDataset(data[i], data));
        }

        return sumOfDeltas / data.length;
    }

    /**
     * Computes the average deviation between the PDF of the dataset and the distribution.
     *
     * @param data          the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return the average deviation in PDF.
     */
    private static double avgDeviationInPdf(double[] data, PgDistribution pgDistribution) {
        double sumOfDeltas = 0;
        for (int i = 0; i < data.length; i++) {
            sumOfDeltas += Math.abs(pgDistribution.pdf(data[i]) - pdfOfDataset(data[i], data));
        }

        return sumOfDeltas / data.length;
    }

    /**
     * Calculates the deviation between the skewness and kurtosis of the dataset and the distribution.
     *
     * @param data          the input dataset.
     * @param pgDistribution the distribution to compare against.
     * @return the combined deviation in skewness and kurtosis.
     */
    private static double deviationInSkewAndKurt(double[] data, PgDistribution pgDistribution) {
        DescriptiveStatistics statistics = new DescriptiveStatistics(data);
        double kurt1 = pgDistribution.kurtosis();
        double skew1 = pgDistribution.skewness();

        double kurt2 = statistics.getKurtosis();
        double skew2 = statistics.getSkewness();

        return sqrt(pow((skew1 - skew2), 2) + pow((kurt1 - kurt2), 2));
    }

    /**
     * Computes the cumulative distribution function (CDF) of the dataset at a given point.
     *
     * @param x    the point at which to evaluate the CDF.
     * @param data the dataset.
     * @return the CDF value at the specified point.
     */
    private static double cdfOfDataset(double x, double[] data) {
        int count = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] <= x) {
                count++;
            }
        }

        return (double) count / data.length;
    }

    /**
     * Computes the probability density function (PDF) of the dataset at a given point.
     * The PDF is estimated using histogram binning.
     *
     * @param x    the point at which to evaluate the PDF.
     * @param data the dataset.
     * @return the PDF value at the specified point.
     */
    private static double pdfOfDataset(double x, double[] data) {
        int bins = 50;
        double min = Arrays.stream(data).min().getAsDouble();
        double max = Arrays.stream(data).max().getAsDouble();
        double binWidth = (max - min) / bins;

        int binIndex = (int) ((x - min) / binWidth);
        if (binIndex < 0 || binIndex >= bins) {
            return 0.0;
        }

        int countInBin = 0;
        for (double value : data) {
            int currentBin = (int) ((value - min) / binWidth);
            if (currentBin == binIndex) {
                countInBin++;
            }
        }

        return (double) countInBin / (data.length * binWidth);
    }
}
