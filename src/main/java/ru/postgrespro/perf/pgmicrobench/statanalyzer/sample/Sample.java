package ru.postgrespro.perf.pgmicrobench.statanalyzer.sample;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * {@code Sample} class represents collection of numerical values with optional weights.
 * It provides various statistical operations, including mean, min, max and ability to sort values.
 * Additionally, it supports arithmetic operations on all values and parsing from string representations.
 */

@Getter
public class Sample implements Iterable<Double> {
    protected final List<Double> values;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(
            values.stream().mapToDouble(Double::doubleValue).toArray());

    @Getter(lazy = true)
    private final List<Double> sortedValues = values.stream().sorted().toList();

    public Sample(List<Double> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Values cannot be empty");
        }

        // copy to unmodifiable list
        this.values = Collections.unmodifiableList(values);
    }

    /**
     * Computes quantile value for given probability.
     *
     * @param quantile desired quantile
     * @return value at specified quantile
     * @throws IllegalArgumentException if quantile is outside range [0, 1]
     */
    public double getQuantile(double quantile) {
        if (quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException("Quantile must be between 0 and 1");
        }
        List<Double> sortedValues = getSortedValues();
        int index = (int) Math.floor(quantile * (sortedValues.size() - 1));
        return sortedValues.get(index);
    }

    /**
     * Returns size of sample.
     *
     * @return number of elements in sample.
     */
    public int size() {
        return values.size();
    }

    /**
     * Returns minimum value in sample.
     *
     * @return minimum value.
     */
    public double getMin() {
        return getDescriptiveStatistics().getMin();
    }

    /**
     * Returns maximum value in sample.
     *
     * @return maximum value.
     */
    public double getMax() {
        return getDescriptiveStatistics().getMax();
    }

    @Override
    public Iterator<Double> iterator() {
        return values.iterator();
    }

    public double getSkewness() {
        return getDescriptiveStatistics().getSkewness();
    }

    public double getKurtosis() {
        return getDescriptiveStatistics().getKurtosis();
    }
}
