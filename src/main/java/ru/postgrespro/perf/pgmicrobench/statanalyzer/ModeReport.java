package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.Data;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The ModeReport class encapsulates the results of the analysis for a
 * specific mode detected in a dataset.
 */
@Data
public class ModeReport {
    final long size;
    final double location;
    final double leftBound;
    final double rightBound;
    final FittedDistribution bestDistribution;
    final List<FittedDistribution> fittedDistributions;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Mode Report (Summary) ===\n")
                .append(String.format(Locale.US,
                        "Mode bounds: [%.2f, %.2f]\n", leftBound, rightBound))
                .append(String.format(Locale.US,
                        "Location: %.6f\n", location))
                .append(String.format(Locale.US,
                        "Mode size: %d\n", size))
                .append("Best Distribution:\n")
                .append(String.format(Locale.US,
                        "  Type: %s\n", bestDistribution.getType()))
                .append(String.format(Locale.US,
                        "  Parameters: %s\n", Arrays.toString(bestDistribution.getParameters())))
                .append(String.format(Locale.US,
                        "  p-value: %s\n", bestDistribution.getPValue()));

        return sb.toString();
    }


    public String toStringVerbose() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Mode Report ===\n")
                .append(String.format(Locale.US,
                        "Mode bounds: [%.2f, %.2f]\n", leftBound, rightBound))
                .append(String.format(Locale.US,
                        "Location: %.6f\n", location))
                .append(String.format(Locale.US,
                        "Mode size: %d\n", size))
                .append("Best Distribution:\n")
                .append(String.format(Locale.US,
                        "  Type: %s\n", bestDistribution.getType()))
                .append(String.format(Locale.US,
                        "  Parameters: %s\n", Arrays.toString(bestDistribution.getParameters())))
                .append(String.format(Locale.US,
                        "  p-value: %s\n", bestDistribution.getPValue()));

        sb.append("\nAll Fitted Distributions:\n");
        for (int i = 0; i < fittedDistributions.size(); i++) {
            FittedDistribution dist = fittedDistributions.get(i);
            sb.append(String.format(Locale.US,
                            "  #%d\n", i + 1))
                    .append(String.format(Locale.US,
                            "    Type: %s\n", dist.getType()))
                    .append(String.format(Locale.US,
                            "    Parameters: %s\n", Arrays.toString(dist.getParameters())))
                    .append(String.format(Locale.US,
                            "    p-value: %s\n", dist.getPValue()));
        }

        return sb.toString();
    }

}
