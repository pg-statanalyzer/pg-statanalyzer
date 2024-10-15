package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.util.ArrayList;
import java.util.List;

public class QuantileRespectfulDensityHistogramBuilder implements IDensityHistogramBuilder {
    private static final QuantileRespectfulDensityHistogramBuilder INSTANCE =
            new QuantileRespectfulDensityHistogramBuilder();

    private QuantileRespectfulDensityHistogramBuilder() {
    }

    public static QuantileRespectfulDensityHistogramBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public DensityHistogram build(Sample sample, int binCount) {
        return build(sample, binCount, null);
    }

    public DensityHistogram build(Sample sample, int binCount, IQuantileEstimator quantileEstimator) {
        if (sample == null) throw new IllegalArgumentException("Sample cannot be null");
        if (binCount <= 1) throw new IllegalArgumentException("binCount must be greater than 1");

        quantileEstimator = (quantileEstimator != null) ?
                quantileEstimator : HarrellDavisQuantileEstimator.getInstance();

        if (sample.isWeighted && !quantileEstimator.supportsWeightedSamples()) {
            throw new WeightedSampleNotSupportedException();
        }

        double[] probabilityValues = new ArithmeticProgressionSequence(0, 1.0 / binCount)
                .generateArray(binCount + 1);

        List<Double> probabilities = Probability.toProbabilities(probabilityValues);
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
}
