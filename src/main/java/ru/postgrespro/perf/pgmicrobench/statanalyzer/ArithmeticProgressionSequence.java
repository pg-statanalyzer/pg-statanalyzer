package ru.postgrespro.perf.pgmicrobench.statanalyzer;

public class ArithmeticProgressionSequence {
    private final double start;
    private final double step;

    public ArithmeticProgressionSequence(double start, double step) {
        this.start = start;
        this.step = step;
    }

    public double value(int index) {
        return start + index * step;
    }

    public double[] generateArray(int size) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = value(i);
        }
        return result;
    }
}
