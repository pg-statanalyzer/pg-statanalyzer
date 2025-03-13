package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.HarrellDavisQuantileEstimator;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.IQuantileEstimator;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.exceptions.WeightedSampleNotSupportedException;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sequences.ArithmeticProgressionSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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
    public DensityHistogram build(Sample sample, int binCount) {
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
    public DensityHistogram build(Sample sample,
                                  int binCount,
                                  IQuantileEstimator quantileEstimator) {
        if (sample == null) {
            throw new IllegalArgumentException("Sample cannot be null");
        }
        if (binCount <= 1) {
            throw new IllegalArgumentException("binCount must be greater than 1");
        }

        quantileEstimator = (quantileEstimator != null)
                ? quantileEstimator : HarrellDavisQuantileEstimator.getInstance();

        if (sample.isWeighted() && !quantileEstimator.supportsWeightedSamples()) {
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
