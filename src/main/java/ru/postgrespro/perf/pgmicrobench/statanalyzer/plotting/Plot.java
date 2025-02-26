package ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting;

import org.knowm.xchart.Histogram;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * The Plot class provides methods to create and display histograms and density functions
 * for a given set of data points.
 */
public class Plot {
    /**
     * Plots a histogram for the given collection of double values.
     *
     * @param sample a collection of Double values to be plotted as a histogram.
     */
    public static void plot(Sample sample) {
        int bins = (int) Math.sqrt(sample.size()) + 1;

        Histogram histogram = new Histogram(sample.getValues(), bins);

        XYChart chart = new XYChart(800, 600);
        chart.addSeries("Гистограмма", histogram.getxAxisData(), histogram.getyAxisData())
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.StepArea)
                .setMarker(SeriesMarkers.NONE);

        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));
        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));

        new SwingWrapper<>(chart).displayChart();
    }

    public static List<Double> filterBinsAbovePdf(Sample sample, Function<Double, Double> pdf) {

        int bins = (int) Math.sqrt(sample.size()) + 1;
        Histogram histogram = new Histogram(sample.getValues(), bins);

        List<Double> filteredData = new ArrayList<>();

        Random random = new Random();
        int totalDataCount = sample.getValues().size();

        for (int i = 0; i < histogram.getxAxisData().size(); i++) {
            double binCenter = histogram.getxAxisData().get(i);
            double binHeight = histogram.getyAxisData().get(i);
            double binWidth = (i + 1 < histogram.getxAxisData().size()) ?
                    histogram.getxAxisData().get(i + 1) - binCenter : 0;

            double normalizedBinHeight = binHeight / (totalDataCount * binWidth);
            double pdfValue = pdf.apply(binCenter);

//            System.out.println("Bin Center: " + binCenter);
//            System.out.println("Bin Height: " + binHeight);
//            System.out.println("PDF: " + pdfValue);

            double leftBound = histogram.getxAxisData().get(i);
            double rightBound = (i + 1 < histogram.getxAxisData().size())
                    ? histogram.getxAxisData().get(i + 1)
                    : leftBound + (leftBound - histogram.getxAxisData().get(i - 1));

            List<Double> binValues = new ArrayList<>();
            for (Double value : sample.getValues()) {
                if (value >= leftBound && value < rightBound) {
                    binValues.add(value);
                }
            }

//            double ratio = pdfValue / normalizedBinHeight;

            if (normalizedBinHeight >= pdfValue) {
                filteredData.addAll(binValues);
            } else {
                if (binValues.size() > 2) {
                    Collections.shuffle(binValues, random);
                    filteredData.addAll(binValues.subList(0, 2));
                } else {
                    filteredData.addAll(binValues);
                }
            }
        }
        return filteredData;
    }

    /**
     * Plots a histogram and a density function for the given collection of double values.
     *
     * @param sample a collection of Double values to be plotted as a histogram.
     * @param pdf    a function that defines the density to be plotted alongside the histogram.
     */
    public static void plot(Sample sample, Function<Double, Double> pdf, String title) {
        int bins = (int) Math.sqrt(sample.size()) + 1;

        Histogram histogram = new Histogram(sample.getValues(), bins);

        double delta = (histogram.getMax() - histogram.getMin()) / bins;
        double cur = histogram.getMin();

        double[] xFunction = new double[2 * bins];
        double[] yFunction = new double[2 * bins];

        for (int i = 0; i < xFunction.length; i++) {
            xFunction[i] = cur;
            yFunction[i] = pdf.apply(cur);
            cur += delta / 2.;
        }

        List<Double> yHistogram = histogram.getyAxisData();
        for (int i = 0; i < yFunction.length / 2; i++) {
            yHistogram.set(i, yHistogram.get(i) / delta / sample.size());
        }

        XYChart chart = new XYChart(800, 600);
        chart.setTitle(title);

        chart.getStyler().setLegendVisible(false);

        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(false);

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
