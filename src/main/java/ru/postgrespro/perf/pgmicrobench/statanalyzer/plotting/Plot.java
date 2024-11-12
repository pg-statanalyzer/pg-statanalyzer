package ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting;

import org.knowm.xchart.Histogram;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * The Plot class provides methods to create and display histograms and density functions
 * for a given set of data points.
 */
public class Plot {
    /**
     * Plots a histogram for the given array of double values.
     *
     * @param data an array of double values to be plotted as a histogram.
     */
    public static void plot(double[] data) {
        ArrayList<Double> dataList = new ArrayList<>();
        for (double datum : data) {
            dataList.add(datum);
        }
        plot(dataList);
    }

    /**
     * Plots a histogram for the given collection of double values.
     *
     * @param data a collection of Double values to be plotted as a histogram.
     */
    public static void plot(Collection<Double> data) {
        int bins = (int) Math.sqrt(data.size()) + 1;

        Histogram histogram = new Histogram(data, bins);

        XYChart chart = new XYChart(800, 600);
        chart.addSeries("Гистограмма", histogram.getxAxisData(), histogram.getyAxisData())
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.StepArea)
                .setMarker(SeriesMarkers.NONE);

        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));
        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));

        new SwingWrapper<>(chart).displayChart();
    }

    /**
     * Plots a histogram and a density function for the given array of double values.
     *
     * @param data an array of double values to be plotted as a histogram.
     * @param pdf  a function that defines the density to be plotted alongside the histogram.
     */
    public static void plot(double[] data, Function<Double, Double> pdf) {
        ArrayList<Double> dataList = new ArrayList<>();
        for (double datum : data) {
            dataList.add(datum);
        }
        plot(dataList, pdf);
    }

    /**
     * Plots a histogram and a density function for the given collection of double values.
     *
     * @param data a collection of Double values to be plotted as a histogram.
     * @param pdf  a function that defines the density to be plotted alongside the histogram.
     */
    public static void plot(Collection<Double> data, Function<Double, Double> pdf) {
        int bins = (int) Math.sqrt(data.size()) + 1;

        Histogram histogram = new Histogram(data, bins);

        double delta = (histogram.getMax() - histogram.getMin()) / bins;
        double cur = histogram.getMin();

        double[] xFunction = new double[bins];
        double[] yFunction = new double[bins];

        for (int i = 0; i < xFunction.length; i++) {
            xFunction[i] = cur;
            yFunction[i] = pdf.apply(cur);
            cur += delta;
        }

        List<Double> yHistogram = histogram.getyAxisData();
        for (int i = 0; i < yFunction.length; i++) {
            yHistogram.set(i, yHistogram.get(i) / delta / data.size());
        }

        XYChart chart = new XYChart(800, 600);

        chart.addSeries("Histogram", histogram.getxAxisData(), histogram.getyAxisData())
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.StepArea)
                .setMarker(SeriesMarkers.NONE);

        chart.addSeries("Function", xFunction, yFunction)
                .setMarker(SeriesMarkers.NONE)
                .setLineColor(java.awt.Color.RED)
                .setLineWidth(2.0f);

        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));
        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));

        new SwingWrapper<>(chart).displayChart();
    }
}
