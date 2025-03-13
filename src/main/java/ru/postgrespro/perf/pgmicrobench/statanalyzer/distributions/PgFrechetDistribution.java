package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.CramerVonMises;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PgFrechetDistribution implements PgSimpleDistribution {
    private static final double ZERO = 1e-6;

    private final double shape;
    private final double scale;
    private final double ratio;

    public PgFrechetDistribution(double shape, double scale) {
        if (shape < ZERO || scale < ZERO) {
            throw new IllegalArgumentException("Incorrect parameters");
        }

        this.shape = shape;
        this.scale = scale;
        this.ratio = shape / scale;
    }

    @Override
    public double mean() {
        return 0;
    }

    @Override
    public double variance() {
        return 0;
    }

    @Override
    public double median() {
        return 0;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return 0;
    }

    @Override
    public double standardDeviation() {
        return PgSimpleDistribution.super.standardDeviation();
    }

    @Override
    public double pdf(double value) {
        double v = value / scale;
        double x = Math.pow(v, -shape);
        return ratio * (x / v) * Math.exp(-x);
    }

    @Override
    public double cdf(double value) {
        double v = value / scale;
        double x = Math.pow(v, -shape);
        return Math.exp(-x);
    }

    @Override
    public Sample generate(int size, Random random) {
        return new Sample(IntStream.range(0, size)
                .mapToObj(i -> {
                    double u = random.nextDouble();
                    return scale * Math.pow(Math.log(1 / u), -1.0 / shape);
                }).collect(Collectors.toList()));
    }

    @Override
    public PgDistributionType getType() {
        return null;
    }

    @Override
    public int getParamNumber() {
        return 2;
    }

    @Override
    public PgDistribution newDistribution(double[] params) {
        return new PgFrechetDistribution(params[0], params[1]);
    }

    @Override
    public double[] getParamArray() {
        return new double[]{shape, scale};
    }

    @Override
    public Pair<double[]> bounds() {
        return new Pair<>(new double[]{ZERO, ZERO},
                new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY});
    }

    @Override
    public String toString() {
        return String.format("Frechet(shape=%.3g, scale=%.3g)", shape, scale);
    }

    public static void main(String[] args) {
        List<Double> data = new ArrayList<>();
        PgSimpleDistribution f = new PgFrechetDistribution(10, 100);

        Sample sample = f.generate(20000, new Random(12));


        AnalysisResult analysisResult = new StatAnalyzer().analyze(sample.getValues());
        PgCompositeDistribution compositeDistribution = analysisResult.getCompositeDistribution();

        PgCompositeDistribution n = (PgCompositeDistribution) (new CramerVonMises().fit(sample, compositeDistribution).getDistribution());

        for (ModeReport mr : analysisResult.getModeReports()) {
            for (FittedDistribution fd : mr.getFittedDistributions()) {
                Plot.plot(sample, fd.getDistribution()::pdf, fd.getDistribution().toString(), true);
            }
        }

        Plot.plot(sample, n::pdf, "", true);
        System.out.println(n);
        System.out.println(new CramerVonMises().test(sample, n));
    }
}
