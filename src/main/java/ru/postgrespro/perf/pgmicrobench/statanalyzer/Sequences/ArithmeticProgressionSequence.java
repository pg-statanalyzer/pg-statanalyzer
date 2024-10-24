package ru.postgrespro.perf.pgmicrobench.statanalyzer.sequences;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Represents arithmetic progression sequence, defined by starting value and constant step.
 * This class provides methods to compute specific values in sequence and generate arrays
 * containing specified number of sequence elements.
 */

@Getter
@RequiredArgsConstructor
public class ArithmeticProgressionSequence {

    private final double start;
    private final double step;

    /**
     * Computes value at specified index in sequence.
     *
     * @param index position in sequence (0-based).
     * @return value of sequence at given index.
     */
    public double value(int index) {
        return start + index * step;
    }

    /**
     * Generates array containing the first {@code size} elements of arithmetic progression.
     *
     * @param size number of elements to generate.
     * @return array containing generated sequence values.
     * @throws IllegalArgumentException if {@code size} is negative.
     */
    public double[] generateArray(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = value(i);
        }
        return result;
    }
}
