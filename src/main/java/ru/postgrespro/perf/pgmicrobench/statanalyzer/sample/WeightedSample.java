package ru.postgrespro.perf.pgmicrobench.statanalyzer.sample;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;

import java.util.Collections;
import java.util.List;

public class WeightedSample extends Sample {
    private final List<Double> weights;

    public WeightedSample(List<Double> values, List<Double> weights) {
        super(values);

        if (values.size() != weights.size()) {
            throw new IllegalArgumentException("Values and weights must have the same length");
        }

        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight < 1e-9) {
            throw new IllegalArgumentException("Total weight must be positive.");
        }

        this.weights = weights.stream().map(it -> it / totalWeight).toList();
    }

    public static WeightedSample evenWeightedSample(List<Double> values) {
        return new WeightedSample(values, Collections.nCopies(values.size(), 1.0 / values.size()));
    }

    /**
     * Returns the weight for a specified histogram bin.
     *
     * @param bin the histogram bin to calculate weight for.
     * @return total weight of the sample values falling within the bin range.
     */
    public double getWeightForBin(DensityHistogramBin bin) {
        double totalWeightForBin = 0.0;
        double binLower = bin.getLower();
        double binUpper = bin.getUpper();

        for (int i = 0; i < values.size(); i++) {
            double value = values.get(i);
            if (value >= binLower && value <= binUpper) {
                totalWeightForBin += weights.get(i);
            }
        }
        return totalWeightForBin;
    }

    public List<Double> getSortedWeights() {
    }
}
