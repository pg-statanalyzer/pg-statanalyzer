package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * {@code Sample} class represents collection of numerical values with optional weights.
 * It provides various statistical operations, including mean, min, max, and ability to sort values.
 * Additionally, it supports arithmetic operations on all values and parsing from string representations.
 */

public class Sample {

    private static final String DEFAULT_FORMAT = "G";
    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';
    private static final char SEPARATOR = ',';

    private final List<Double> values;
    private final List<Double> weights;
    private final double totalWeight;
    public final boolean isWeighted;
    private final Lazy<List<Double>> lazySortedValues;
    private final Lazy<List<Double>> lazySortedWeights;

    /**
     * Helper class for lazy initialization of values using {@link Supplier}.
     */
    private static class Lazy<T> {
        private T value;
        private final Supplier<T> supplier;

        public Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (value == null) {
                value = supplier.get();
            }
            return value;
        }
    }

    public double getWeightForBin(DensityHistogramBin bin) {
        double totalWeightForBin = 0.0;
        double binLower = bin.lower();
        double binUpper = bin.upper();

        for (int i = 0; i < values.size(); i++) {
            double value = values.get(i);
            if (value >= binLower && value <= binUpper) {
                totalWeightForBin += weights.get(i);
            }
        }
        return totalWeightForBin;
    }

    /**
     * Constructs {@code Sample} with specified array of values, assuming equal weights.
     *
     * @param values array of sample values.
     */
    public Sample(double[] values) {
        this(Arrays.stream(values).boxed().collect(Collectors.toList()), null);
    }

    /**
     * Constructs {@code Sample} with specified list of values and equal weights.
     *
     * @param values list of sample values.
     * @throws IllegalArgumentException if values are null or empty.
     */
    public Sample(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values cannot be null or empty");
        }

        this.values = values;
        double weight = 1.0 / values.size();
        this.weights = Collections.nCopies(values.size(), weight);
        this.totalWeight = 1.0;
        this.isWeighted = false;

        this.lazySortedValues = new Lazy<>(() -> sortList(values));
        this.lazySortedWeights = new Lazy<>(() -> weights);
    }

    /**
     * Constructs weighted {@code Sample} with specified values and weights.
     *
     * @param values  list of sample values.
     * @param weights list of corresponding weights.
     * @throws IllegalArgumentException if values or weights are null, empty, or of unequal length.
     */
    public Sample(List<Double> values, List<Double> weights) {
        if (values == null || values.isEmpty() || weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("Values and weights cannot be null or empty");
        }
        if (values.size() != weights.size()) {
            throw new IllegalArgumentException("Values and weights must have the same length");
        }

        this.values = values;
        this.weights = weights;
        this.totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (this.totalWeight < 1e-9) {
            throw new IllegalArgumentException("Total weight must be positive.");
        }
        this.isWeighted = true;

        this.lazySortedValues = new Lazy<>(() -> sortList(values));
        this.lazySortedWeights = new Lazy<>(() -> sortList(weights));
    }

    /**
     * Returns sorted values.
     *
     * @return list of sorted values.
     */
    public List<Double> getSortedValues() {
        return lazySortedValues.get();
    }

    /**
     * Returns sorted weights.
     *
     * @return list of sorted weights.
     */
    public List<Double> getSortedWeights() {
        return lazySortedWeights.get();
    }

    /**
     * Returns size of sample.
     *
     * @return number of elements in sample.
     */
    public int getSize() {
        return values.size();
    }

    /**
     * Returns weighted size of sample.
     *
     * @return effective size based on weights.
     */
    public double getWeightedSize() {
        return totalWeight * totalWeight / weights.stream().mapToDouble(w -> w * w).sum();
    }

    /**
     * Returns mean of sample values.
     *
     * @return mean value, or NaN if sample is empty.
     */
    public double getMean() {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    /**
     * Returns minimum value in sample.
     *
     * @return minimum value.
     */
    public double getMin() {
        return getSortedValues().get(0);
    }

    /**
     * Returns maximum value in sample.
     *
     * @return maximum value.
     */
    public double getMax() {
        return getSortedValues().get(getSortedValues().size() - 1);
    }

    /**
     * Returns total weight of sample.
     *
     * @return total weight.
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Concatenates current sample with another sample.
     *
     * @param other other sample to concatenate.
     * @return new {@code Sample} containing combined values and weights.
     */
    public Sample concat(Sample other) {
        List<Double> newValues = new ArrayList<>(this.values);
        newValues.addAll(other.values);

        List<Double> newWeights = new ArrayList<>(this.weights);
        newWeights.addAll(other.weights);

        return new Sample(newValues, newWeights);
    }

    /**
     * Adds constant value to all elements of sample.
     *
     * @param value value to add.
     * @return new {@code Sample} with modified values.
     */
    public Sample add(double value) {
        List<Double> newValues = values.stream().map(v -> v + value).collect(Collectors.toList());
        return isWeighted ? new Sample(newValues, weights) : new Sample(newValues);
    }

    /**
     * Multiplies all elements of sample by constant value.
     *
     * @param value multiplier.
     * @return new {@code Sample} with modified values.
     */
    public Sample multiply(double value) {
        List<Double> newValues = values.stream().map(v -> v * value).collect(Collectors.toList());
        return isWeighted ? new Sample(newValues, weights) : new Sample(newValues);
    }

    public static boolean tryParse(String s, SampleHolder holder) {
        try {
            if (!s.startsWith(String.valueOf(OPEN_BRACKET)) || !s.contains(String.valueOf(CLOSE_BRACKET))) {
                return false;
            }

            int openBracketIndex = s.indexOf(OPEN_BRACKET);
            int closeBracketIndex = s.indexOf(CLOSE_BRACKET);
            String[] valueStrings = s.substring(openBracketIndex + 1, closeBracketIndex).split(String.valueOf(SEPARATOR));
            List<Double> values = Arrays.stream(valueStrings)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());

            holder.sample = new Sample(values);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses {@code Sample} from string representation.
     *
     * @param s string representation of sample.
     * @return parsed {@code Sample}.
     * @throws IllegalArgumentException if format is invalid.
     */
    public static Sample parse(String s) {
        SampleHolder holder = new SampleHolder();
        if (tryParse(s, holder)) {
            return holder.sample;
        } else {
            throw new IllegalArgumentException("Invalid sample format");
        }
    }

    /**
     * Helper class for holding {@code Sample} instance during parsing.
     */
    public static class SampleHolder {
        public Sample sample;
    }

    private List<Double> sortList(List<Double> list) {
        List<Double> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList);
        return sortedList;
    }
}
