package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * {@code Sample} class represents collection of numerical values with optional weights.
 * It provides various statistical operations, including mean, min, max and ability to sort values.
 * Additionally, it supports arithmetic operations on all values and parsing from string representations.
 */

@Getter
public class Sample implements Iterable<Double> {

    private static final String DEFAULT_FORMAT = "G";
    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';
    private static final char SEPARATOR = ',';

    private final List<Double> values;
    private final List<Double> weights;
    private final double totalWeight;
    private final boolean isWeighted;
    private final Lazy<List<Double>> lazySortedValues;
    private final Lazy<List<Double>> lazySortedWeights;
    private final Lazy<DescriptiveStatistics> lazyDescriptiveStatistics;


    /**
     * Constructs a {@code Sample} with specified array of values, without taking into account weights.
     *
     * @param values array of sample values.
     */
    public Sample(double[] values) {
        this(Arrays.stream(values).boxed().collect(Collectors.toList()), false, null);
    }

    /**
     * Constructs {@code Sample} with specified array of values, without taking into account weights.
     *
     * @param values list of sample values.
     */
    public Sample(List<Double> values) {
        this(values, false, null);
    }

    /**
     * Constructs {@code Sample} with specified list of values and equal weights.
     *
     * @param values list of sample values.
     * @throws IllegalArgumentException if values are null or empty.
     */
    public Sample(@NonNull List<Double> values, Boolean isWeighted) {
        this(values, isWeighted, isWeighted ? Collections.nCopies(values.size(), 1.0 / values.size()) : null);
    }

    /**
     * Constructs weighted {@code Sample} with specified values and weights.
     *
     * @param values  list of sample values.
     * @param weights list of corresponding weights.
     * @throws IllegalArgumentException if values or weights are null, empty, or of unequal length.
     */
    public Sample(@NonNull List<Double> values, @NonNull List<Double> weights) {
        this(values, true, weights);
    }

    private Sample(List<Double> values, Boolean isWeighted, List<Double> weights) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Values cannot be empty");
        }
        this.values = values;
        this.weights = weights;
        this.isWeighted = isWeighted;

        if (isWeighted) {
            if (weights.isEmpty()) {
                throw new IllegalArgumentException("Weights cannot be empty");
            }
            if (values.size() != weights.size()) {
                throw new IllegalArgumentException("Values and weights must have the same length");
            }
            this.totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
            if (this.totalWeight < 1e-9) {
                throw new IllegalArgumentException("Total weight must be positive.");
            }
        } else {
            this.totalWeight = 0;
        }

        this.lazySortedValues = new Lazy<>(() -> sortList(values));
        this.lazySortedWeights = new Lazy<>(() -> sortList(weights));
        this.lazyDescriptiveStatistics = new Lazy<>(() -> {
            double[] arrayValues = new double[size()];
            for (int i = 0; i < size(); i++) {
                arrayValues[i] = values.get(i);
            }
            return new DescriptiveStatistics(arrayValues);
        });
    }

    /**
     * Try to parse.
     */
    public static boolean tryParse(String s, SampleHolder holder) {
        try {
            if (!s.startsWith(String.valueOf(OPEN_BRACKET)) || !s.contains(String.valueOf(CLOSE_BRACKET))) {
                return false;
            }

            int openBracketIndex = s.indexOf(OPEN_BRACKET);
            int closeBracketIndex = s.indexOf(CLOSE_BRACKET);
            String[] valueStrings = s.substring(openBracketIndex + 1, closeBracketIndex).split(String.valueOf(SEPARATOR));
            List<Double> values = Arrays.stream(valueStrings).map(Double::parseDouble).collect(Collectors.toList());

            holder.sample = new Sample(values, true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public double getQuantile(double quantile) {
        if (quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException("Quantile must be between 0 and 1");
        }
        List<Double> sortedValues = lazySortedValues.get();
        int index = (int) Math.floor(quantile * (sortedValues.size() - 1));
        return sortedValues.get(index);
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
     * Get value.
     *
     * @param index position of value.
     * @return value.
     */
    public Double get(int index) {
        return values.get(index);
    }

    /**
     * Weighted or not.
     */
    public boolean isWeighted() {
        return isWeighted;
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
        return getSortedValues().get(0);
    }

    /**
     * Returns maximum value in sample.
     *
     * @return maximum value.
     */
    public double getMax() {
        return getSortedValues().get(size() - 1);
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

    private List<Double> sortList(List<Double> list) {
        List<Double> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList);
        return sortedList;
    }

    @Override
    public Iterator<Double> iterator() {
        return values.iterator();
    }

    public double getSkewness() {
        return lazyDescriptiveStatistics.get().getSkewness();
    }

    public double getKurtosis() {
        return lazyDescriptiveStatistics.get().getKurtosis();
    }

    /**
     * Helper class for lazy initialization of values using {@link Supplier}.
     */
    private static class Lazy<T> {
        private final Supplier<T> supplier;
        private T value;

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

    /**
     * Helper class for holding {@code Sample} instance during parsing.
     */
    public static class SampleHolder {
        public Sample sample;
    }
}
