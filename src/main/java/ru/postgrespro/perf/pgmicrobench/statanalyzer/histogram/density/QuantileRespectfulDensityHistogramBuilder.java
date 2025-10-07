package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.HarrellDavisQuantileEstimator;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.IQuantileEstimator;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.exceptions.WeightedSampleNotSupportedException;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.WeightedSample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sequences.ArithmeticProgressionSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


/**
 * Builder for constructing {@link DensityHistogram} where bins respect quantiles of given sample.
 * This class follows Singleton pattern, and histogram can be created using quantile estimators.
 */

@NoArgsConstructor(staticName = "getInstance")
public class QuantileRespectfulDensityHistogramBuilder implements IDensityHistogramBuilder {

    @Getter
    private static final QuantileRespectfulDensityHistogramBuilder INSTANCE =
            new QuantileRespectfulDensityHistogramBuilder();

    /**
     * Builds {@link DensityHistogram} with specified number of bins using default quantile estimator.
     *
     * @param sample   {@link Sample} from which histogram is built. Must not be {@code null}.
     * @param binCount number of bins to create. Must be greater than 1.
     * @return {@link DensityHistogram} built from provided sample.
     * @throws IllegalArgumentException if sample is {@code null} or {@code binCount} is less than or equal to 1.
     */
    @Override
    public DensityHistogram build(WeightedSample sample, int binCount) {
        return build(sample, binCount, null);
    }

    /**
     * Builds {@link DensityHistogram} with specified number of bins, using provided quantile estimator.
     * If no estimator is provided, {@link HarrellDavisQuantileEstimator} is used by default.
     *
     * @param sample            {@link Sample} from which histogram is built. Must not be {@code null}.
     * @param binCount          number of bins to create. Must be greater than 1.
     * @param quantileEstimator {@link IQuantileEstimator} to use. If {@code null}, default estimator is used.
     * @return {@link DensityHistogram} built from provided sample.
     * @throws IllegalArgumentException            if sample is {@code null} or {@code binCount} is less than or equal to 1.
     * @throws WeightedSampleNotSupportedException if sample is weighted and estimator doesn't support weighted samples.
     */
    public DensityHistogram build(@NonNull WeightedSample sample,
                                  int binCount,
                                  IQuantileEstimator quantileEstimator) {
        if (binCount <= 1) {
            throw new IllegalArgumentException("binCount must be greater than 1");
        }

        quantileEstimator = (quantileEstimator != null)
                ? quantileEstimator : HarrellDavisQuantileEstimator.getInstance();

        if (!quantileEstimator.supportsWeightedSamples()) {
            throw new WeightedSampleNotSupportedException();
        }

        double[] probabilityValues = new ArithmeticProgressionSequence(0,
                1.0 / binCount)
                .generateArray(binCount + 1);

        List<Double> probabilities = Arrays.stream(probabilityValues)
                .boxed()
                .collect(Collectors.toList());

        double[] quantiles = quantileEstimator.quantiles(sample, probabilities);

        List<DensityHistogramBin> bins = new ArrayList<>(binCount);
        for (int i = 0; i < binCount; i++) {
            double width = quantiles[i + 1] - quantiles[i];
            if (width > 1e-9) {
                double value = 1.0 / binCount / width;
                bins.add(new DensityHistogramBin(quantiles[i], quantiles[i + 1], value));
            }
        }

        return new DensityHistogram(bins);
    }

    /**
     * Computes PDF values for given list of x-values
     * using provided {@link DensityHistogram}
     *
     * <p>Method iterates over each x-value and determines which histogram bin it falls into.
     * Corresponding bin height is assigned as PDF value. If x-value does not fall
     * within any bin range, its PDF value is set to zero
     *
     * @param xValues list of x-values for which PDF should be computed
     * @param histogram density histogram containing bins with height values
     * @return list of y-values representing computed PDF values for given x-values
     */
    public static List<Double> computePdf(List<Double> xValues, DensityHistogram histogram) {
        List<Double> yValues = new ArrayList<>();

        for (double x : xValues) {
            double y = 0.0;
            for (DensityHistogramBin bin : histogram.getBins()) {
                if (x >= bin.getLower() && x < bin.getUpper()) {
                    y = bin.getHeight();
                    break;
                }
            }
            yValues.add(y);
        }
        return yValues;
    }
}
