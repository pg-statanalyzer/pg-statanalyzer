package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    public Sample(double[] values) {
        this(Arrays.stream(values).boxed().collect(Collectors.toList()), null);
    }

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

    private List<Double> sortList(List<Double> list) {
        List<Double> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList);
        return sortedList;
    }

    public List<Double> getSortedValues() {
        return lazySortedValues.get();
    }

    public List<Double> getSortedWeights() {
        return lazySortedWeights.get();
    }

    public int getSize() {
        return values.size();
    }

    public double getWeightedSize() {
        return totalWeight * totalWeight / weights.stream().mapToDouble(w -> w * w).sum();
    }

    public double getMean() {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    public double getMin() {
        return getSortedValues().get(0);
    }

    public double getMax() {
        return getSortedValues().get(getSortedValues().size() - 1);
    }

    // Добавленный метод
    public double getTotalWeight() {
        return totalWeight;
    }

    public Sample concat(Sample other) {
        List<Double> newValues = new ArrayList<>(this.values);
        newValues.addAll(other.values);

        List<Double> newWeights = new ArrayList<>(this.weights);
        newWeights.addAll(other.weights);

        return new Sample(newValues, newWeights);
    }

    public Sample concatWithoutWeights(Sample other) {
        List<Double> newValues = new ArrayList<>(this.values);
        newValues.addAll(other.values);
        return new Sample(newValues);  // Создаём новый Sample без весов
    }

    @Override
    public String toString() {
        return toString(null, Locale.getDefault());
    }

    public String toString(String format, Locale locale) {
        format = format != null ? format : DEFAULT_FORMAT;
        NumberFormat numberFormat = NumberFormat.getInstance(locale);

        StringBuilder builder = new StringBuilder();
        builder.append(OPEN_BRACKET);
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                builder.append(SEPARATOR);
            }
            builder.append(numberFormat.format(values.get(i)));
        }
        builder.append(CLOSE_BRACKET);
        return builder.toString();
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

    public static class SampleHolder {
        public Sample sample;
    }

    public static Sample parse(String s) {
        SampleHolder holder = new SampleHolder();
        if (tryParse(s, holder)) {
            return holder.sample;
        } else {
            throw new IllegalArgumentException("Invalid sample format");
        }
    }

    public Sample add(double value) {
        List<Double> newValues = values.stream().map(v -> v + value).collect(Collectors.toList());
        return isWeighted ? new Sample(newValues, weights) : new Sample(newValues);
    }

    public Sample subtract(double value) {
        List<Double> newValues = values.stream().map(v -> v - value).collect(Collectors.toList());
        return isWeighted ? new Sample(newValues, weights) : new Sample(newValues);
    }

    public Sample multiply(double value) {
        List<Double> newValues = values.stream().map(v -> v * value).collect(Collectors.toList());
        return isWeighted ? new Sample(newValues, weights) : new Sample(newValues);
    }

    public Sample divide(double value) {
        if (value == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        List<Double> newValues = values.stream().map(v -> v / value).collect(Collectors.toList());
        return isWeighted ? new Sample(newValues, weights) : new Sample(newValues);
    }

    // Перегрузки для int значений
    public Sample add(int value) {
        return add((double) value);
    }

    public Sample subtract(int value) {
        return subtract((double) value);
    }

    public Sample multiply(int value) {
        return multiply((double) value);
    }

    public Sample divide(int value) {
        return divide((double) value);
    }
}
