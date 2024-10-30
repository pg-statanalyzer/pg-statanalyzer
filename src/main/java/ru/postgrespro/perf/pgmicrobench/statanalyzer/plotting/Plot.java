package ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.knowm.xchart.Histogram;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Plot {
	public static void main(String[] args) {
		LogNormalDistribution normalDistribution = new LogNormalDistribution(1, 0.5);

		double[] data = normalDistribution.sample(100000);

		List<Double> dataList = new ArrayList<>();
		for (double datum : data) {
			dataList.add(datum);
		}

		plot(dataList, normalDistribution::density);
	}

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

	public static void plot(Collection<Double> data, Function<Double, Double> function) {
		int bins = (int) Math.sqrt(data.size()) + 1;

		Histogram histogram = new Histogram(data, bins);

		double delta = (histogram.getMax() - histogram.getMin()) / bins;
		double cur = histogram.getMin();

		double[] xFunction = new double[bins];
		double[] yFunction = new double[bins];

		for (int i = 0; i < xFunction.length; i++) {
			xFunction[i] = cur;
			yFunction[i] = function.apply(cur);
			cur += delta;
		}

		List<Double> yHistogram = histogram.getyAxisData();
		for (int i = 0; i < yFunction.length; i++) {
			yHistogram.set(i, yHistogram.get(i) / delta / data.size());
		}

		System.out.println(histogram.getyAxisData());

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
