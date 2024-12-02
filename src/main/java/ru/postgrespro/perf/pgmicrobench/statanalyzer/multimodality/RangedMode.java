package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;

import lombok.Getter;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.util.List;


/**
 * Represents mode within specific range, with information about its location and sample data it covers.
 * This class provides details about mode's position and allows access to sample's values.
 */

@Getter
public class RangedMode {

    /**
     * -- GETTER --
     * Returns location (center point) of mode.
     */
    private final double location;

    /**
     * -- GETTER --
     * Returns left boundary of mode's range.
     */
    private final double left;

    /**
     * -- GETTER --
     * Returns right boundary of mode's range.
     */
    private final double right;

    /**
     * -- GETTER --
     * Returns sample data associated with mode.
     */
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
                location, left, right, sample.size()
        );
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
