package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.math3.util.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RecursiveLowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.WeightedSample;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Statalt {

    private static final Double TEST_SIZE = 0.3;

    @Builder.Default
    private final List<PgSimpleDistribution> findInDistributions = Stream.of(
                    new PgLogNormalDistribution(1, 0.5),
                    new PgGumbelDistribution(1, 1),
                    new PgWeibullDistribution(1, 1),
                    new PgFrechetDistribution(5, 1))
            .toList();
    @Builder.Default
    private final LowlandModalityDetector modeDetector = new LowlandModalityDetector(0.5, 0.01, false);
    @Builder.Default
    private final IDistributionTest distributionTest = new CramerVonMises();
    @Builder.Default
    private final IParameterEstimator parameterEstimator = new CramerVonMises();
    @Builder.Default
    private final IParameterEstimator finalParameterEstimator = new CramerVonMises();
    @Builder.Default
    private final boolean optimizeFinalSolution = false;
    @Builder.Default
    private final boolean useJittering = false;
    @Builder.Default
    private final boolean recursiveModeDetection = false;
    @Builder.Default
    private final Random random = new Random();
    @Builder.Default
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    private static Sample findModeValues(Sample sample, RangedMode mode) {
        return new Sample(sample.getValues().stream()
                .filter((x) -> x >= mode.getLeft() && x <= mode.getRight())
                .collect(Collectors.toList()));
    }

    /**
     * Analyzes the given list of values to detect modes and fit
     * distributions.
     *
     * @param values a list of latency values to analyze
     * @return an AnalysisResult containing the results of the analysis
     */
    public AnalysisResult analyze(List<Double> values) {
        if (useJittering) {
            Jittering jit = new Jittering();
            values = jit.jitter(values, random);
        }

        WeightedSample sample = WeightedSample.evenWeightedSample(values);

        ModalityData modalityData = modeDetector.detectModes(sample);

        List<List<PgSimpleDistribution>> combinations = combinations(modalityData.getModality());

        List<Double> weigths = new ArrayList<>(modalityData.getModality());

        for (int j = 0; j < modalityData.getModality(); j++) {
            RangedMode mode = modalityData.getModes().get(j);
            Sample modeSample = findModeValues(sample, mode);

            for (List<PgSimpleDistribution> combination : combinations) {
                combination.replaceAll(distribution -> distribution.newDistribution(modeSample));
            }

            weigths.add(modeSample.size() / (double) sample.size());
        }

        List<PgCompositeDistribution> compositeDistributions = combinations.stream().map(it -> new PgCompositeDistribution(
                        it.stream().collect(Collectors.toUnmodifiableList()), weigths))
                .toList();

        Pair<Sample, Sample> paramTest = splitParamsTest(sample);

        EstimatedParameters ep = compositeDistributions.parallelStream()
                .map(distribution -> finalParameterEstimator.fit(paramTest.getFirst(), distribution))
                .max(Comparator.comparingDouble(EstimatedParameters::getPValue))
                .orElseGet(null);

        System.out.println(distributionTest.test(paramTest.getSecond(), ep.getDistribution()));

        return new AnalysisResult(
                modalityData.getModality(),
                null,
                (PgCompositeDistribution) ep.getDistribution());
    }

    /**
     * Splits the given sample into two samples for parameter estimating, and testing.
     *
     * @param sample the sample to split
     * @return a Pair containing the parameter sample and test sample
     */
    public Pair<Sample, Sample> splitParamsTest(Sample sample) {
        List<Double> shuffled = new ArrayList<>(sample.getValues());
        Collections.shuffle(shuffled, random);
        int testSize = (int) (shuffled.size() * TEST_SIZE);

        Sample paramsSample = new Sample(
                IntStream.range(0, testSize)
                        .mapToObj(shuffled::get)
                        .collect(Collectors.toList()));
        Sample testSample = new Sample(
                IntStream.range(testSize, shuffled.size())
                        .mapToObj(shuffled::get)
                        .collect(Collectors.toList()));

        return new Pair<>(paramsSample, testSample);
    }

    private List<List<PgSimpleDistribution>> combinations(int i) {
        int comb = (int) Math.pow(findInDistributions.size(), i);
        List<List<PgSimpleDistribution>> combinations = new ArrayList<>(comb);

        for (int j = 0; j < comb; j++) {
            int cur = j;
            List<PgSimpleDistribution> distributions = new ArrayList<>(findInDistributions.size());
            for (int k = 0; k < i; k++) {
                distributions.add(findInDistributions.get(cur % findInDistributions.size()));
                cur = cur / findInDistributions.size();
            }

            combinations.add(distributions);
        }

        return combinations;
    }
}
