package ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting;

import org.knowm.xchart.Histogram;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.QuantileRespectfulDensityHistogramBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        List<Double> histogramX = histogram.getxAxisData();
        for (int i = 0; i < yFunction.length / 2; i++) {
            histogramX.set(i, histogramX.get(i) - delta);
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

    /**
     * Filters bins from histogram where estimated density is significantly higher than provided PDF.
     * This function smooths transitions across detected gaps to maintain data continuity
     *
     * @param sample input sample containing values to be processed
     * @param pdf    PDF used for comparison
     * @return filtered list of values that respect estimated PDF constraints
     */
    public static List<Double> filterBinsAbovePdf(Sample sample, Function<Double, Double> pdf) {

        int bins = (int) Math.sqrt(sample.size()) + 1;
        Histogram histogram = new Histogram(sample.getValues(), bins);

        List<Double> filteredData = new ArrayList<>();

        int totalDataCount = sample.getValues().size();

        List<Double> lastBinValues = new ArrayList<>();
        boolean inGap = false;
        double lastValueBeforeGap = Double.NaN;
        double nextValueAfterGap;

        for (int i = 0; i < histogram.getxAxisData().size(); i++) {
            double binCenter = histogram.getxAxisData().get(i);
            double binHeight = histogram.getyAxisData().get(i);
            double binWidth = (i + 1 < histogram.getxAxisData().size()) ?
                    histogram.getxAxisData().get(i + 1) - binCenter : 0;

            double normalizedBinHeight = binHeight / (totalDataCount * binWidth);
            double pdfValue = pdf.apply(binCenter);

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

            if (normalizedBinHeight >= pdfValue) {
                double ratio = pdfValue / normalizedBinHeight;

                if (ratio < 0.45) {
                    filteredData.addAll(binValues);
                    lastBinValues = new ArrayList<>(binValues);
                    inGap = false;
                } else {
                    if (!inGap) {
                        inGap = true;
                        lastValueBeforeGap = lastBinValues.isEmpty() ? Double.NaN : lastBinValues.get(lastBinValues.size() - 1);
                    }
                }
            } else {
                if (inGap) {
                    nextValueAfterGap = binValues.isEmpty() ? Double.NaN : binValues.get(0);
                    if (!Double.isNaN(lastValueBeforeGap) && !Double.isNaN(nextValueAfterGap)) {
                        filteredData.addAll(generateSmoothTransition(lastValueBeforeGap, nextValueAfterGap, 100));
                    }
                    inGap = false;
                }
            }
        }
        return filteredData;
    }

    /**
     * Generates smooth transition between two values using linear interpolation with slight sinusoidal adjustment.
     * This helps in filling gaps in data while maintaining natural-looking distribution
     *
     * @param start starting value of transition
     * @param end   ending value of transition
     * @param steps number of interpolation steps
     * @return list of interpolated values forming smooth transition
     */
    private static List<Double> generateSmoothTransition(double start, double end, int steps) {
        List<Double> transition = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1);
            double value = start * (1 - t) + end * t;
            value += Math.sin(t * Math.PI) * (end - start) * 0.1;
            transition.add(value);
        }
        return transition;
    }

    /**
     * Plots QRDE-HD for given sample.
     *
     * @param sample sample data used to construct histogram
     */
    public static void plotQuantileHistogram(Sample sample) {
        int binCount = (int) Math.sqrt(sample.size()) + 1;
        QuantileRespectfulDensityHistogramBuilder builder = QuantileRespectfulDensityHistogramBuilder.getInstance();
        DensityHistogram densityHistogram = builder.build(sample, binCount);

        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        for (DensityHistogramBin bin : densityHistogram.getBins()) {
            double left = bin.getLower();
            double right = bin.getUpper();
            double value = bin.getHeight();

            xData.add(left);
            yData.add(value);
            xData.add(right);
            yData.add(value);
        }

        XYChart chart = new XYChart(800, 600);
        chart.setTitle("QRDE");

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(false);

        chart.addSeries("Quantile Histogram", xData, yData)
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Step)
                .setMarker(SeriesMarkers.NONE)
                .setLineWidth(2.0f);

        chart.getStyler().setxAxisTickLabelsFormattingFunction(value -> String.format("%.2f", value));

        new SwingWrapper<>(chart).displayChart();
    }
}
