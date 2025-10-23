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

/**
 * Application class.
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StatAnalyzer {
    private static final Double TEST_SIZE = 0.5;

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
    private final IParameterEstimator finalParameterEstimator = new KolmogorovSmirnov();
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

    /**
     * Creates a composite distribution from a list of mode reports.
     *
     * @param modeReports list of mode reports obtained from the sample analysis
     * @param sampleSize  the size of the original sample
     * @return a PgCompositeDistribution composed of individual distributions and their
     * corresponding weights computed based on the sample proportions
     */
    private static PgCompositeDistribution getCompositeDistribution(List<ModeReport> modeReports, int sampleSize) {
        List<PgDistribution> distributions = new ArrayList<>(modeReports.size());
        List<Double> weights = new ArrayList<>(modeReports.size());
        for (ModeReport modeReport : modeReports) {
            weights.add(modeReport.size / (double) sampleSize);
            distributions.add(modeReport.bestDistribution.getDistribution());
        }

        return new PgCompositeDistribution(distributions, weights);
    }

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

        ModalityData modalityData = findModes(sample);

        List<ModeReport> modeReports = getModeReports(sample, modalityData);

        PgCompositeDistribution compositeDistribution = getCompositeDistribution(modeReports, values.size());

        if (optimizeFinalSolution) {
            EstimatedParameters estimatedParameters = finalParameterEstimator.fit(sample, compositeDistribution);
            compositeDistribution = (PgCompositeDistribution) estimatedParameters.getDistribution();
        }

        if (!recursiveModeDetection) {
            return new AnalysisResult(modalityData.getModality(), modeReports, compositeDistribution);
        }

        return recursiveModeDetection(sample, compositeDistribution, modalityData, modeReports, values.size());
    }

    /**
     * Performs recursive mode detection by filtering data points below PDF.
     * Detecting additional modes and refining composite distribution
     *
     * @param sample              original sample containing data points
     * @param initialDistribution initial composite distribution before recursive detection
     * @param initialModalityData initial modality data detected before recursion
     * @param modeReports         list to store mode reports found during analysis
     * @param originalSampleSize  original size of sample before filtering
     * @return {@code AnalysisResult} containing updated modality data, mode reports and refined composite distribution
     */
    private AnalysisResult recursiveModeDetection(Sample sample, PgCompositeDistribution initialDistribution,
                                                  ModalityData initialModalityData, List<ModeReport> modeReports,
                                                  int originalSampleSize) {
        final double MODE_SIZE_THRESHOLD = 0.07;

        List<Double> filteredSampleData = RecursiveLowlandModalityDetector.filterBinsAbovePdf(sample, initialDistribution::pdf);
        WeightedSample filteredSample = WeightedSample.evenWeightedSample(filteredSampleData);

        ModalityData newModalityData = findModes(filteredSample);
        List<ModeReport> newModeReports = getModeReports(filteredSample, newModalityData);

        newModeReports.removeIf(mode -> mode.getSize() < originalSampleSize * MODE_SIZE_THRESHOLD);

        if (newModeReports.isEmpty()) {
            return new AnalysisResult(initialModalityData.getModality(), modeReports, initialDistribution);
        }

        modeReports.addAll(newModeReports);

        PgCompositeDistribution newCompositeDistribution = getCompositeDistribution(newModeReports, filteredSampleData.size());

        if (optimizeFinalSolution) {
            EstimatedParameters optimizedParameters = finalParameterEstimator.fit(filteredSample, newCompositeDistribution);
            newCompositeDistribution = (PgCompositeDistribution) optimizedParameters.getDistribution();
        }

        PgCompositeDistribution combinedDistribution = combinePdfWithScaling(
                initialDistribution, newCompositeDistribution,
                filteredSampleData.size(), originalSampleSize
        );

        EstimatedParameters optimizedParameters = finalParameterEstimator.fit(sample, combinedDistribution);
        combinedDistribution = (PgCompositeDistribution) optimizedParameters.getDistribution();

        return new AnalysisResult(
                modeReports.size(),
                modeReports, combinedDistribution
        );
    }

    /**
     * Combines original PDF with weighted sum of PDFs.
     *
     * @param totalModeSize total size of modes, which is used to calculate weight of lowland PDF
     * @param sampleSize    size of original sample, which is used to calculate weight of original PDF
     * @return new function that represents combined PDF with scaled contributions
     */
    public PgCompositeDistribution combinePdfWithScaling(
            PgCompositeDistribution originalDistribution,
            PgCompositeDistribution lowlandDistribution,
            long totalModeSize,
            long sampleSize) {

        double totalSize = sampleSize + totalModeSize;
        double weightOriginal = sampleSize / totalSize;
        double weightLowland = totalModeSize / totalSize;

        List<PgDistribution> combinedDistributions = new ArrayList<>();
        List<Double> combinedWeights = new ArrayList<>();

        combinedDistributions.addAll(originalDistribution.getDistributions());
        for (double weight : originalDistribution.getWeights()) {
            combinedWeights.add(weight * weightOriginal);
        }

        combinedDistributions.addAll(lowlandDistribution.getDistributions());
        for (double weight : lowlandDistribution.getWeights()) {
            combinedWeights.add(weight * weightLowland);
        }

        return new PgCompositeDistribution(combinedDistributions, combinedWeights);
    }

    /**
     * Generates mode reports for each detected mode in parallel.
     *
     * @param modalityData the modality data containing detected modes
     * @return a list of ModeReport objects for each ranged mode
     */
    private List<ModeReport> getModeReports(Sample sample, ModalityData modalityData) {
        return modalityData.getModes().stream()
                .map(mode -> CompletableFuture.supplyAsync(() -> getModeReport(sample, mode), pool))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * Finds the modes in the given sample using the mode detector.
     *
     * @param sample the sample from which to detect modes
     * @return a ModalityData object containing the detected modes
     */
    public ModalityData findModes(WeightedSample sample) {
        return modeDetector.detectModes(sample);
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

    /**
     * Generates a report for the specified mode based on the fitted
     * distributions.
     *
     * @param mode the mode for which to generate the report
     * @return a ModeReport containing the results for the mode
     */
    public ModeReport getModeReport(Sample sample, RangedMode mode) {
        Sample modeSample = findModeValues(sample, mode);
        Pair<Sample, Sample> paramsTest = splitParamsTest(modeSample);
        Sample paramsSample = paramsTest.getFirst();
        Sample testSample = paramsTest.getSecond();

        List<FittedDistribution> fittedDistributions = fitDistribution(paramsSample, testSample);
        FittedDistribution bestDistribution = fittedDistributions.get(0);

        return new ModeReport(modeSample.size(), mode.getLocation(), mode.getLeft(), mode.getRight(), bestDistribution, fittedDistributions);
    }

    /**
     * Fits various distributions to the provided parameter and test samples.
     *
     * @param parametersSample the sample used for estimating parameters
     * @param testSample       the sample used for testing the fit
     * @return a list of FittedDistribution objects containing the fitted
     * distributions and their p-values
     */
    public List<FittedDistribution> fitDistribution(Sample parametersSample, Sample testSample) {
        return findInDistributions.stream()
                .map(distribution -> CompletableFuture.supplyAsync(() -> {
                    EstimatedParameters estimatedParameters;
                    try {
                        estimatedParameters = parameterEstimator.fit(parametersSample,
                                distribution.newDistribution(parametersSample));
                    } catch (Exception e) {
                        return new FittedDistribution(null, Double.NEGATIVE_INFINITY);
                    }

                    double pValue = distributionTest.test(testSample, estimatedParameters.getDistribution());

                    return new FittedDistribution(
                            estimatedParameters.getDistribution(),
                            pValue);
                }, pool))
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingDouble(FittedDistribution::getPValue).reversed())
                .collect(Collectors.toList());
    }
}
