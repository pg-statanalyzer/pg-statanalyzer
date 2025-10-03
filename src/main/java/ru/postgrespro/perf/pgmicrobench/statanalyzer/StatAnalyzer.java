package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Application class.
 */
@Getter
@Setter
@NoArgsConstructor
public class StatAnalyzer {
    public static final List<PgSimpleDistribution> supportedDistributions = new ArrayList<>();
    private static final Double TEST_SIZE = 0.8;

    static {
//        supportedDistributions.add(new PgNormalDistribution(1, 1));
        supportedDistributions.add(new PgLogNormalDistribution(1, 0.5));
        supportedDistributions.add(new PgGumbelDistribution(1, 1));
        supportedDistributions.add(new PgWeibullDistribution(1, 1));
        supportedDistributions.add(new PgFrechetDistribution(2, 1));
    }

    private LowlandModalityDetector modeDetector = new LowlandModalityDetector(0.5, 0.01, false);
    private IDistributionTest distributionTest = new CramerVonMises();
    private IParameterEstimator parameterEstimator = new CramerVonMises();
    private IParameterEstimator finalParameterEstimator = new KolmogorovSmirnov();
    private boolean optimizeFinalSolution = false;
    private boolean useJittering = false;
    private boolean recursiveModeDetection = false;
    private Random random = new Random(42);

    /**
     * Constructs a StatAnalyzer with the specified parameter estimator
     * and distribution test.
     *
     * @param parameterEstimator the parameter estimator to be used
     * @param distributionTest   the distribution test to be used
     */
    public StatAnalyzer(IParameterEstimator parameterEstimator, IDistributionTest distributionTest) {
        this.parameterEstimator = parameterEstimator;
        this.distributionTest = distributionTest;
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
        Sample filteredSample = new Sample(filteredSampleData, true);

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

    /**
     * Generates mode reports for each detected mode in parallel.
     *
     * @param modalityData the modality data containing detected modes
     * @return a list of ModeReport objects for each ranged mode
     */
    private List<ModeReport> getModeReports(Sample sample, ModalityData modalityData) {
        List<CompletableFuture<ModeReport>> futures = new ArrayList<>(modalityData.getModes().size());
        for (RangedMode mode : modalityData.getModes()) {
            futures.add(CompletableFuture.supplyAsync(() -> getModeReport(sample, mode)));
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * Finds the modes in the given sample using the mode detector.
     *
     * @param sample the sample from which to detect modes
     * @return a ModalityData object containing the detected modes
     */
    public ModalityData findModes(Sample sample) {
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
        List<CompletableFuture<FittedDistribution>> futures = new ArrayList<>(supportedDistributions.size());

        for (PgSimpleDistribution simpleDistribution : supportedDistributions) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                EstimatedParameters estimatedParameters;
                try {
                    estimatedParameters = parameterEstimator.fit(parametersSample, simpleDistribution);
                } catch (Exception e) {
                    System.out.println("WARNING: cant find parameters for " + simpleDistribution);
                    return new FittedDistribution(null, Double.NEGATIVE_INFINITY);
                }

                double pValue = distributionTest.test(testSample, estimatedParameters.getDistribution());

                return new FittedDistribution(
                        estimatedParameters.getDistribution(),
                        pValue);
            }));
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingDouble(FittedDistribution::getPValue).reversed())
                .collect(Collectors.toList());
    }

    private static Sample findModeValues(Sample sample, RangedMode mode) {
        return new Sample(sample.getValues().stream()
                .filter((x) -> x >= mode.getLeft() && x <= mode.getRight())
                .collect(Collectors.toList()));
    }
}
