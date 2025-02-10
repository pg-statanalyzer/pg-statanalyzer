package ru.postgrespro.perf.pgmicrobench.statanalyzer.changepoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of the ED-PELT (Empirical Distribution Pruned Exact Linear Time) change point detection algorithm.
 * Based on "Haynes, K., Fearnhead, P., & Eckley, I. A. (2017). A Computationally Efficient Nonparametric Approach for
 * Changepoint Detection."
 */

public class EdPeltChangePointDetector {

    /**
     * Detects change points in a given dataset using the ED-PELT algorithm.
     *
     * @param data The input time series data.
     * @return A list of detected change point indexes.
     */
    public static List<Integer> getChangePointIndexes(List<Double> data) {
        return getChangePointIndexes(data, 1);
    }

    /**
     * Detects change points with a specified minimum segment length.
     *
     * @param data The input time series data.
     * @param minDistance Minimum allowed distance between change points.
     * @return A list of detected change point indexes.
     */
    public static List<Integer> getChangePointIndexes(List<Double> data, int minDistance) {
        return getChangePointIndexes(data.stream().mapToDouble(Double::doubleValue).toArray(), minDistance);
    }

    /**
     * Detects change points in a given dataset using the ED-PELT algorithm.
     *
     * @param data The input time series data.
     * @return A list of detected change point indexes.
     */
    public static List<Integer> getChangePointIndexes(double[] data) {
        return getChangePointIndexes(data, 1);
    }

    /**
     * Detects change points with a specified minimum segment length.
     *
     * @param data The input time series data.
     * @param minDistance Minimum allowed distance between change points.
     * @return A list of detected change point indexes.
     */
    public static List<Integer> getChangePointIndexes(double[] data, int minDistance) {
        int n = data.length;

        if (n <= 2) {
            return new ArrayList<>();
        }
        if (minDistance < 1 || minDistance > n) {
            throw new IllegalArgumentException("minDistance must be between 1 and n");
        }

        double penalty = 3 * Math.log(n);

        int k = Math.min(n, (int) Math.ceil(4 * Math.log(n)));

        int[][] partialSums = getPartialSums(data, k);

        double[] bestCost = new double[n + 1];
        bestCost[0] = -penalty;
        for (int currentTau = minDistance; currentTau < 2 * minDistance; currentTau++) {
            bestCost[currentTau] = getSegmentCost(partialSums, 0, currentTau, k, n);
        }

        int[] previousChangePointIndex = new int[n + 1];
        List<Integer> previousTaus = new ArrayList<>(n + 1);
        previousTaus.add(0);
        previousTaus.add(minDistance);
        List<Double> costForPreviousTau = new ArrayList<>(n + 1);

        for (int currentTau = 2 * minDistance; currentTau < n + 1; currentTau++) {
            costForPreviousTau.clear();

            for (int previousTau : previousTaus) {
                costForPreviousTau.add(bestCost[previousTau] + getSegmentCost(partialSums, previousTau, currentTau, k, n) + penalty);
            }

            int bestPreviousTauIndex = whichMin(costForPreviousTau);
            bestCost[currentTau] = costForPreviousTau.get(bestPreviousTauIndex);
            previousChangePointIndex[currentTau] = previousTaus.get(bestPreviousTauIndex);

            double currentBestCost = bestCost[currentTau];
            int newPreviousTausSize = 0;
            for (int i = 0; i < previousTaus.size(); i++) {
                if (costForPreviousTau.get(i) < currentBestCost + penalty) {
                    previousTaus.set(newPreviousTausSize++, previousTaus.get(i));
                }
            }

            previousTaus.subList(newPreviousTausSize, previousTaus.size()).clear();

            previousTaus.add(currentTau - (minDistance - 1));
        }

        List<Integer> changePointIndexes = new ArrayList<>();
        int currentIndex = previousChangePointIndex[n];

        while (currentIndex != 0) {
            changePointIndexes.add(currentIndex - 1);
            currentIndex = previousChangePointIndex[currentIndex];
        }
        Collections.reverse(changePointIndexes);

        return changePointIndexes;
    }

    private static int[][] getPartialSums(double[] data, int k) {
        int n = data.length;
        int[][] partialSums = new int[k][n + 1];
        double[] sortedData = Arrays.stream(data).sorted().toArray();

        for (int i = 0; i < k; i++) {
            double z = -1 + (2 * i + 1.0) / k; // Values from (-1+1/k) to (1-1/k) with step = 2/k
            double p = 1.0 / (1 + Math.pow(2 * n - 1, -z)); // Values from 0.0 to 1.0
            double t = sortedData[(int) Math.floor((n - 1) * p)]; // Quantile value, formula (2.1) in [Haynes2017]

            for (int tau = 1; tau <= n; tau++) {
                partialSums[i][tau] = partialSums[i][tau - 1];
                if (data[tau - 1] < t)
                    partialSums[i][tau] += 2; // We use doubled value (2) instead of original 1.0
                if (data[tau - 1] == t)
                    partialSums[i][tau] += 1; // We use doubled value (1) instead of original 0.5
            }
        }
        return partialSums;
    }

    private static double getSegmentCost(int[][] partialSums, int tau1, int tau2, int k, int n) {
        double sum = 0;
        for (int i = 0; i < k; i++) {
            // actualSum is (count(data[j] < t) * 2 + count(data[j] == t) * 1) for j=tau1..tau2-1
            int actualSum = partialSums[i][tau2] - partialSums[i][tau1];

            // We skip these two cases (correspond to fit = 0 or fit = 1) because of invalid Math.log values
            if (actualSum != 0 && actualSum != (tau2 - tau1) * 2) {
                // Empirical CDF \hat{F}_i(t) (Section 2.1 "Model" in [Haynes2017])
                double fit = actualSum * 0.5 / (tau2 - tau1);
                // Segment cost \mathcal{L}_{np} (Section 2.2 "Nonparametric maximum likelihood" in [Haynes2017])
                double lnp = (tau2 - tau1) * (fit * Math.log(fit) + (1 - fit) * Math.log(1 - fit));
                sum += lnp;
            }
        }
        double c = -Math.log(2 * n - 1); // Constant from Lemma 3.1 in [Haynes2017]
        return 2.0 * c / k * sum; // See Section 3.1 "Discrete approximation" in [Haynes2017]
    }

    private static int whichMin(List<Double> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Array should contain elements");
        }

        double minValue = values.get(0);
        int minIndex = 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) < minValue) {
                minValue = values.get(i);
                minIndex = i;
            }
        }
        return minIndex;
    }
}
