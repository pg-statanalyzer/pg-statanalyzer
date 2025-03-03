package ru.postgrespro.perf.pgmicrobench.statanalyzer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.*;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.plotting.Plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
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
    private static final PgDistributionType[] supportedDistributions = PgDistributionType.values();
    private LowlandModalityDetector modeDetector = new LowlandModalityDetector(0.5, 0.01, false);
    private IDistributionTest distributionTest = new Multicriteria();
    private IParameterEstimator parameterEstimator = new Multicriteria();

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

    public static void main(String[] args) {
        String file = "distributionSample/SELECT.csv";

        List<Double> dataList = new ArrayList<>(10000);
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextDouble()) {
                double value = scanner.nextDouble() / 100000.0; // division to simplify the search for parameters
                dataList.add(value);

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        StatAnalyzer statAnalyzer = new StatAnalyzer();


        AnalysisResult analysisResult = statAnalyzer.analyze(dataList);
        Function<Double, Double> summaryPdf = analysisResult.getPdf();

        List<Double> filteredData = Plot.filterBinsAbovePdf(new Sample(dataList, true), summaryPdf);

//        List<Double> smoothedData = statAnalyzer.smoothWithMovingAverage(filteredData, 5);
        Sample smoothedSample = new Sample(filteredData, true);

        ModalityData modalityData = statAnalyzer.applyNewLowland(smoothedSample);

        List<ModeReport> modeReports = new ArrayList<>();
        for (RangedMode mode : modalityData.getModes()) {
            Sample modeSample = statAnalyzer.findModeValues(mode, smoothedSample);
            ModeReport modeReport = statAnalyzer.getModeReport(mode, modeSample);
            modeReports.add(modeReport);
        }

        // summaryPdf = normalizePdf(summaryPdf, dataList.size());

        Function<Double, Double> combinedPdf = statAnalyzer.combinePdfWithScaling(summaryPdf, modeReports, dataList.size());

        // combinedPdf = normalizePdf(combinedPdf, smoothedData.size());

        //Plot.plot(new Sample(filteredData), summaryPdf, "Filtered Summary pdf");

        //Plot.plot(new Sample(smoothedData), summaryPdf, "Smoothed Summary pdf");

        Plot.plot(new Sample(dataList), analysisResult.getPdf(), "Summary pdf");
        Plot.plot(new Sample(dataList), combinedPdf, "Combined pdf +");
    }

    public ModalityData applyNewLowland(Sample sample) {
        LowlandModalityDetector newModeDetector = new LowlandModalityDetector(0.9, 0.01, false);

        return newModeDetector.detectModes(sample);
    }

    public Function<Double, Double> combinePdfWithScaling(Function<Double, Double> originalPdf, List<ModeReport> modeReports, long sampleSize) {
        double totalModeSize = modeReports.stream().mapToLong(mode -> mode.size).sum();
        double totalSize = sampleSize + totalModeSize;

        Function<Double, Double> lowlandPdf = (x) -> {
            double result = 0;

            for (ModeReport modeReport : modeReports) {
                double weight = (double) modeReport.size / totalSize;
                result += weight * modeReport.bestDistribution.getDistribution().pdf(x);
            }
            return result;
        };

        return (x) -> (sampleSize / totalSize) * originalPdf.apply(x) + lowlandPdf.apply(x);
    }

    private static Function<Double, Double> normalizePdf(Function<Double, Double> pdf, int sampleSize) {
        double integral = integratePdf(pdf, sampleSize);
        return x -> pdf.apply(x) / integral;
    }

    private static double integratePdf(Function<Double, Double> pdf, int sampleSize) {
        double stepSize = 0.01;
        double sum = 0.0;
        for (double x = 0; x <= 1000; x += stepSize) {
            sum += pdf.apply(x) * stepSize;
        }
        return sum;
    }

    public List<Double> smoothWithMovingAverage(List<Double> data, int windowSize) {
        List<Double> smoothedData = new ArrayList<>(data.size());

        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - windowSize / 2);
            int end = Math.min(data.size() - 1, i + windowSize / 2);

            double average = data.subList(start, end + 1).stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            smoothedData.add(average);
        }

        return smoothedData;
    }

    /**
     * Analyzes the given list of latencies to detect modes and fit
     * distributions.
     *
     * @param latencies a list of latency values to analyze
     * @return an AnalysisResult containing the results of the analysis
     */
    public AnalysisResult analyze(List<Double> latencies) {
        Sample sample = new Sample(latencies, true);

        ModalityData modalityData = findModes(sample);

        List<CompletableFuture<ModeReport>> futures = new ArrayList<>(modalityData.getModes().size());
        for (RangedMode mode : modalityData.getModes()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Sample modeSample = findModeValues(mode, sample);
                return getModeReport(mode, modeSample);
            }));
        }
        List<ModeReport> modeReports = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        Function<Double, Double> summaryPdf = findSummaryPdf(modeReports, sample.size());

        return new AnalysisResult(modalityData.getModes().size(), modeReports, summaryPdf);
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
     * Extracts the values from the sample that fall within the specified
     * mode range.
     *
     * @param mode   the mode range to filter values
     * @param sample the sample from which to extract mode values
     * @return a Sample containing the values within the mode range
     */
    public Sample findModeValues(RangedMode mode, Sample sample) {
        return new Sample(sample.getValues().stream()
                .filter(value -> value >= mode.getLeft() && value <= mode.getRight())
                .collect(Collectors.toList()));

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
     * @param mode       the mode for which to generate the report
     * @param modeSample the sample corresponding to the mode
     * @return a ModeReport containing the results for the mode
     */
    public ModeReport getModeReport(RangedMode mode, Sample modeSample) {
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

        List<CompletableFuture<FittedDistribution>> futures = new ArrayList<>(supportedDistributions.length);

        for (PgDistributionType distributionType : supportedDistributions) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                EstimatedParameters estimatedParameters;
                try {
                    estimatedParameters = parameterEstimator.fit(parametersSample, distributionType);
                } catch (Exception e) {
                    return new FittedDistribution(distributionType, null, null, Double.NEGATIVE_INFINITY);
                }

                double pValue = distributionTest.test(testSample, estimatedParameters.getDistribution());

                return new FittedDistribution(
                        distributionType,
                        estimatedParameters.params,
                        estimatedParameters.getDistribution(),
                        pValue);
            }));
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingDouble(FittedDistribution::getPValue).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Calculates a summary probability density function (PDF) based on
     * the fitted distributions from the provided mode reports.
     *
     * @param modeReports a list of ModeReport objects, each containing
     *                    information about a detected mode and its
     *                    corresponding fitted distribution
     * @param sampleSize  the total size of the sample used for analysis,
     *                    which is used to normalize the weights of the
     *                    distributions
     * @return a Function that takes a double value (x) and returns the
     * calculated summary PDF at that point
     */
    public Function<Double, Double> findSummaryPdf(List<ModeReport> modeReports, long sampleSize) {
        return (x) -> {
            double result = 0;
            for (ModeReport modeReport : modeReports) {
                double weight = modeReport.size / (double) sampleSize;

                result += weight * modeReport.bestDistribution.getDistribution().pdf(x);
            }
            return result;
        };
    }
}
