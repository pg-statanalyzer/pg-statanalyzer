package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Utility class for generating different types of statistical distributions.
 */

public class NormalDistribution {

    /**
     * Generates list of random numbers following normal (Gaussian) distribution.
     *
     * @param size   number of values to generate.
     * @param mean   mean of distribution.
     * @param stdDev standard deviation of distribution.
     * @return list of randomly generated numbers.
     */
    public static List<Double> generate(int size, double mean, double stdDev) {
        Random random = new Random();
        List<Double> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.add(mean + stdDev * random.nextGaussian());
        }
        return data;
    }
}