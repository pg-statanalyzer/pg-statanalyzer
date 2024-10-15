package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import java.util.List;

public class RangedMode {
    private final double location;
    private final double left;
    private final double right;
    private final Sample sample;

    public RangedMode(double location, double left, double right, Sample sample) {
        this.location = location;
        this.left = left;
        this.right = right;
        this.sample = sample;
    }

    @Override
    public String toString() {
        return String.format(
                "Mode at %.2f in range [%.2f, %.2f] with %d points",
                location, left, right, sample.getSize()
        );
    }

    public double getLocation() {
        return location;
    }

    public double getLeft() {
        return left;
    }

    public double getRight() {
        return right;
    }

    public Sample getSample() {
        return sample;
    }

    public List<Double> getValues() {
        return sample.getSortedValues();
    }

    public double getMin() {
        return sample.getSortedValues().get(0);
    }

    public double getMax() {
        return sample.getSortedValues().get(sample.getSortedValues().size() - 1);
    }
}
