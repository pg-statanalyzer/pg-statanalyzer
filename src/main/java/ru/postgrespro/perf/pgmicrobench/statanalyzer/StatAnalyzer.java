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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

    static {
//        supportedDistributions.add(new PgNormalDistribution(1, 1));
        supportedDistributions.add(new PgLogNormalDistribution(1, 0.5));
        supportedDistributions.add(new PgGumbelDistribution(1, 1));
        supportedDistributions.add(new PgWeibullDistribution(1, 1));
    }

    private LowlandModalityDetector modeDetector = new LowlandModalityDetector(0.5, 0.01, false);
    private IDistributionTest distributionTest = new CramerVonMises();
    private IParameterEstimator parameterEstimator = new CramerVonMises();
    private IParameterEstimator finalParameterEstimator = new KolmogorovSmirnov();
    private boolean optimizeFinalSolution = false;
    private boolean useJittering = false;

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
            values = jit.jitter(values, new Random(42));
        }

        Sample sample = new Sample(values, true);

        ModalityData modalityData = findModes(sample);

        List<ModeReport> modeReports = getModeReports(modalityData);

        PgCompositeDistribution compositeDistribution = getCompositeDistribution(modeReports, sample.size());

        if (optimizeFinalSolution) {
            EstimatedParameters estimatedParameters = finalParameterEstimator.fit(sample, compositeDistribution);
            compositeDistribution = (PgCompositeDistribution) estimatedParameters.getDistribution();
        }

        return new AnalysisResult(modalityData.getModality(), modeReports, compositeDistribution);
    }

    /**
     * Combines original PDF with weighted sum of PDFs
     * from detected modes, scaling them based on their respective sizes
     *
     * @param originalPdf   original PDF to be combined
     * @param lowlandPdf    lowland PDF to be combined
     * @param totalModeSize total size of modes, which is used to calculate weight of lowland PDF
     * @param sampleSize    size of original sample, which is used to calculate weight of original PDF
     * @return new function that represents combined PDF with scaled contributions
     */
    public Function<Double, Double> combinePdfWithScaling(
            Function<Double, Double> originalPdf,
            Function<Double, Double> lowlandPdf,
            long totalModeSize,
            long sampleSize) {

        double totalSize = sampleSize + totalModeSize;

        double weightOriginalPdf = sampleSize / totalSize;
       
        return (x) -> originalPdf.apply(x) + weightOriginalPdf * lowlandPdf.apply(x);
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
    private List<ModeReport> getModeReports(ModalityData modalityData) {
        List<CompletableFuture<ModeReport>> futures = new ArrayList<>(modalityData.getModes().size());
        for (RangedMode mode : modalityData.getModes()) {
            futures.add(CompletableFuture.supplyAsync(() -> getModeReport(mode)));
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
        Sample paramsSample = new Sample(
                IntStream.iterate(0, n -> n + 2).limit((sample.size() + 1) / 2)
                        .mapToObj(sample::get)
                        .collect(Collectors.toList()));
        Sample testSample = new Sample(
                IntStream.iterate(1, n -> n + 2).limit(sample.size() / 2)
                        .mapToObj(sample::get)
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
    public ModeReport getModeReport(RangedMode mode) {
        Pair<Sample, Sample> paramsTest = splitParamsTest(mode.getSample());
        Sample paramsSample = paramsTest.getFirst();
        Sample testSample = paramsTest.getSecond();

        List<FittedDistribution> fittedDistributions = fitDistribution(paramsSample, testSample);
        FittedDistribution bestDistribution = fittedDistributions.get(0);

        return new ModeReport(mode.getSample().size(), mode.getLocation(), mode.getLeft(), mode.getRight(), bestDistribution, fittedDistributions);
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
                    System.out.println("WARNING: cant find parmeters for " + simpleDistribution);
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
}
