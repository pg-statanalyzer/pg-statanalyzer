package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Loader analyzer = new Loader();

        List<Double> values = new ArrayList<>();
        values.addAll(generateNormalDistribution(100, 5.0, 1.0));  // N(5, 1)
        values.addAll(generateUniformDistribution(100, 10.0, 15.0));  // U(10, 15)

        double[] latencies = values.stream().mapToDouble(Double::doubleValue).toArray();
        analyzer.loadLatencies(latencies);

        System.out.println("Latency count: " + analyzer.getLatencyCount());

        Sample sample = new Sample(values);
        LowlandModalityDetector detector = new LowlandModalityDetector(0.5, 0.01, false);
        ModalityData result = detector.detectModes(sample);
        System.out.println(result);

        plotHistogram(values, "Histogram", 20);
    }

    // N(μ, σ)
    private static List<Double> generateNormalDistribution(int size, double mean, double stdDev) {
        Random random = new Random();
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(mean + stdDev * random.nextGaussian());
        }
        return data;
    }

    // U(a, b)
    private static List<Double> generateUniformDistribution(int size, double min, double max) {
        Random random = new Random();
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(min + (max - min) * random.nextDouble());
        }
        return data;
    }

    private static void plotHistogram(List<Double> values, String title, int bins) {
        HistogramDataset dataset = new HistogramDataset();
        double[] data = values.stream().mapToDouble(Double::doubleValue).toArray();
        dataset.addSeries("Frequency", data, bins);

        JFreeChart histogram = ChartFactory.createHistogram(
                title, "Value", "Bins",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ChartPanel(histogram));
        frame.pack();
        frame.setVisible(true);
    }
}
