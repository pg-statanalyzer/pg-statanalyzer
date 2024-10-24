package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.List;


/**
 * Represents mode within specific range, with information about its location and sample data it covers.
 * This class provides details about mode's position and allows access to sample's values.
 */

public class RangedMode {

    private final double location;

    private final double left;

    private final double right;

    private final Sample sample;

    /**
     * Constructs {@code RangedMode} instance with specified location, range and sample.
     *
     * @param location center point of mode.
     * @param left     left boundary of mode's range.
     * @param right    right boundary of mode's range.
     * @param sample   {@link Sample} associated with mode.
     */
    public RangedMode(double location, double left, double right, Sample sample) {
        this.location = location;
        this.left = left;
        this.right = right;
        this.sample = sample;
    }

    /**
     * Returns string representation of mode, including its location, range and size of sample.
     *
     * @return string describing mode's properties.
     */
    @Override
    public String toString() {
        return String.format(
                "Mode at %.2f in range [%.2f, %.2f] with %d points",
                location, left, right, sample.getSize()
        );
    }

    /**
     * Returns location (center point) of mode.
     *
     * @return mode's location.
     */
    public double getLocation() {
        return location;
    }

    /**
     * Returns left boundary of mode's range.
     *
     * @return left boundary.
     */
    public double getLeft() {
        return left;
    }

    /**
     * Returns right boundary of mode's range.
     *
     * @return right boundary.
     */
    public double getRight() {
        return right;
    }

    /**
     * Returns sample data associated with mode.
     *
     * @return {@link Sample} associated with this mode.
     */
    public Sample getSample() {
        return sample;
    }

    /**
     * Returns list of sorted values from sample associated with mode.
     *
     * @return list of sorted sample values.
     */
    public List<Double> getValues() {
        return sample.getSortedValues();
    }
}
