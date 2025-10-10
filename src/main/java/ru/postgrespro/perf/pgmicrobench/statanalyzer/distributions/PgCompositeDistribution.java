package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

import lombok.Getter;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PgCompositeDistribution.
 */
public class PgCompositeDistribution implements PgDistribution {

    @Getter
    private final List<PgDistribution> distributions;
    @Getter
    private final List<Double> weights;
    @Getter
    private final int size;
    private final int paramNumber;
    
    /** Constructor.
     *
     * @param distributions distributions.
     * @param weights weights.
     */
    public PgCompositeDistribution(List<PgDistribution> distributions, List<Double> weights) {
        if (distributions.isEmpty() || distributions.size() != weights.size()) {
            throw new IllegalArgumentException("Distributions and weights must have the same non-zero size");
        }
        for (double w : weights) {
            if (w < 0) {
                throw new IllegalArgumentException("Negative weight");
            }
        }

        double sumWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        this.weights = weights.stream().map((x) -> x / sumWeight).collect(Collectors.toList());
        this.distributions = new ArrayList<>(distributions);
        this.size = distributions.size();
        this.paramNumber = this.distributions.stream().mapToInt(PgDistribution::getParamNumber).sum() + size;
    }

    @Override
    public double pdf(double value) {
        return weightedResult((dist) -> dist.pdf(value));
    }

    @Override
    public double cdf(double value) {
        return weightedResult((dist) -> dist.cdf(value));
    }

    @Override
    public Sample generate(int size, Random random) {
        ArrayList<Double> values = new ArrayList<>(size);

        for (int i = 0; i < this.size; i++) {
            values.addAll(distributions.get(i).generate((int) (size * weights.get(i)), random).getValues());
        }

        return new Sample(values);
    }

    @Override
    public PgDistributionType getType() {
        throw new RuntimeException();
    }

    @Override
    public int getParamNumber() {
        return paramNumber;
    }

    @Override
    public PgCompositeDistribution newDistribution(double[] params) {
        if (params.length != paramNumber) {
            throw new IllegalArgumentException();
        }

        List<PgDistribution> newDist = new ArrayList<>(size);
        List<Double> newWeights = new ArrayList<>(size);

        int cur = 0;
        for (PgDistribution d : distributions) {
            double[] p = new double[d.getParamNumber()];
            for (int i = 0; i < p.length; i++) {
                p[i] = params[cur++];
            }
            newDist.add(d.newDistribution(p));
        }

        for (int i = 0; i < size; i++) {
            newWeights.add(params[cur++]);
        }

        return new PgCompositeDistribution(newDist, newWeights);
    }

    @Override
    public double[] getParamArray() {
        double[] paramArray = new double[paramNumber];

        int cur = 0;
        for (PgDistribution d : distributions) {
            for (double p : d.getParamArray()) {
                paramArray[cur++] = p;
            }
        }

        for (double w : weights) {
            paramArray[cur++] = w;
        }

        return paramArray;
    }

    @Override
    public Pair<double[]> bounds() {
        double[] lower = new double[paramNumber];
        double[] upper = new double[paramNumber];

        int cur = 0;
        for (PgDistribution d : distributions) {
            Pair<double[]> bounds = d.bounds();
            for (int i = 0; i < d.getParamNumber(); i++) {
                lower[cur] = bounds.first[i];
                upper[cur++] = bounds.second[i];
            }
        }

        for (int i = 0; i < this.size; i++) {
            lower[cur] = 0;
            upper[cur++] = 1;
        }

        return new Pair<>(lower, upper);
    }

    private double weightedResult(Function<PgDistribution, Double> function) {
        double result = 0;

        for (int i = 0; i < size; i++) {
            result += function.apply(distributions.get(i)) * weights.get(i);
        }

        return result;
    }

    @Override
    public String toString() {
        List<String> list = IntStream.range(0, size)
                .mapToObj(i -> String.format("%.2f", weights.get(i)) + " * " + distributions.get(i))
                .collect(Collectors.toList());
        return "CompositeDistribution(" + String.join(" + ", list) + ")";
    }
}
