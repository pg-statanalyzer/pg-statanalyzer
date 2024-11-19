package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Multicriteria {
    public static FittedDistribution fit(double[] data, PgDistributionType pgDistType) {
        return null;
    }

    private double avgDeviationInCdf(double[] data, PgDistribution pgDistribution) {
        double sumOfDeltas = 0;
        for (int i = 0; i < data.length; i++) {
            sumOfDeltas += Math.abs(pgDistribution.cdf(data[i]) - cdfOfDataset(data[i], data));
        }

        return sumOfDeltas / data.length;
    }

    private double avgDeviationInPdf(double[] data, PgDistribution pgDistribution) {
        double sumOfDeltas = 0;
        for (int i = 0; i < data.length; i++) {
            sumOfDeltas += Math.abs(pgDistribution.pdf(data[i]) - pdfOfDataset(data[i], data));
        }

        return sumOfDeltas / data.length;
    }

    private double deviationInSkewAndKurt(double[] data, PgDistribution pgDistribution) {
        DescriptiveStatistics statistics = new DescriptiveStatistics(data);
        double kurt1 = pgDistribution.kurtosis();
        double skew1 = pgDistribution.skewness();

        double kurt2 = statistics.getKurtosis();
        double skew2 = statistics.getSkewness();

        return sqrt(pow((skew1 - skew2), 2) + pow((kurt1 - kurt2), 2));
    }

    private double cdfOfDataset(double x, double[] data) {
        int count = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] <= x) {
                count++;
            }
        }

        return (double) count / data.length;
    }

    public static double pdfOfDataset(double x, double[] data) {
        int bins = 50;
        double min = Arrays.stream(data).min().getAsDouble();
        double max = Arrays.stream(data).max().getAsDouble();
        double binWidth = (max - min) / bins;

        int binIndex = (int) ((x - min) / binWidth);
        if (binIndex < 0 || binIndex >= bins) {
            return 0.0;
        }

        int countInBin = 0;
        for (double value : data) {
            int currentBin = (int) ((value - min) / binWidth);
            if (currentBin == binIndex) {
                countInBin++;
            }
        }

        return (double) countInBin / (data.length * binWidth);
    }
}
