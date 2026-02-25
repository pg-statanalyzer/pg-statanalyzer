# pg-statanalyzer

## Overview

Statistical analysis library for benchmark/result samples in Java — detection of multimodality, mode splitting, and distribution fitting.
Given a sample — a list of numeric values — the library detects if the data is multimodal, splits the sample into separate modes, fits probability distributions to each mode, estimates distribution parameters via numerical optimization, and evaluates goodness-of-fit using statistical tests.
As a result, you will get a report with information about every found mode: its bounds and best fitted distribution.

## Glossary
1. **Sample** - A set of numerical observations collected for analysis, typically representing measurements from an experiment or benchmark.
2. **Multimodality** - A property of a dataset where its distribution contains two or more distinct peaks (modes), indicating multiple underlying behaviors or processes.
3. **Probability distribution** — A mathematical function that describes how likely different values of a random variable are. It defines the overall shape and behavior of the data.
4. **Criteria (goodness-of-fit criterion)** — A statistical method used to measure how well a chosen probability distribution matches the observed data. Examples include the Kolmogorov–Smirnov and Cramér–von Mises criteria.
5. **p-value** — A statistical indicator that measures how compatible the observed data is with the null hypothesis; lower p-values imply stronger evidence that the data does not follow the expected model.
6. **Jittering** — A technique where small random noise is added to data points to break ties or smooth the sample, often used to improve visualization or statistical processing.

## How It Works (High-Level Algorithm)

1. **Preprocessing:** 
 - Put your sample into our `Sample` class, which will generate lazy methods for ordinary statistical metrics, such as mean, median, std, etc.
 - Optionally, you can use the implementation of Jittering, if your data is too discrete. Used statistical methods will be more objective after jittering :)
2. **Multimodality detection:**
 - Next, your prepared sample will go through Lowland Multimodality Detector, which is described in the original [article by Andrey Akinshin "Lowland Multimodality Detection"](https://aakinshin.net/posts/lowland-multimodality-detection/)
 - It will detect all modes in your sample and split them into different unimodal samples, preparing them for the next step.
 - Optionally, you can use Recursive Mode Detection, which will firstly detect the biggest modes, delete them from the sample, and then run the algorithm again until no modes are left to detect.
4. **Distribution fitting**
 - For each detected mode:
   1. Split the mode sample into two subsamples: one for parameters estimation and one for distribution evaluation.
   2. Assume that the sample follows distribution X, then it will optimize parameters based on distribution X by maximizing p-value of the chosen criterion (Kolmogorov, CVM, etc.). For these purposes we use subsample for parameters.
   3. After optimizing parameters, let's call them (a,b), calculate the p-value of the chosen criterion for distribution X with parameters (a, b)
   4. The resulting p-value will be considered final 
      - If it is more than 0.05, we consider that distribution X fits your sample well. 
      - If it is less than 0.05, we consider that this distribution does not fit your sample.
 - If no distribution achieves a p-value above 0.05, none of the supported distributions fit your sample.
 - If we have only one distribution with p-value more than 0.05, we consider it the best and a good fit.
 - If more than one distribution has a p-value above 0.05, you may choose whichever one fits your needs best.
5. **Result**
 - We provide a report where you can find:
   - Number of detected modes
   - For each mode
     - Its bounds
     - Number of values in the mode
     - Best fitted distribution with parameters
     - p-value 

### Input
A `List<Double>` object with your sample. 

This could be a set of raw latency values from your benchmark or a list of TPS (transactions per second) measurements collected during the test.

## Usage Examples

### Minimal Example

```java
    // put your sample here
    List<Double> dataList = new ArrayList<>(30000);
    
    Sample sample = new Sample(dataList);

    StatAnalyzer statAnalyzer = StatAnalyzer.builder().build();

    AnalysisResult analysisResult = statAnalyzer.analyze(dataList);

    PgCompositeDistribution compositeDistribution = analysisResult.compositeDistribution;

    for (var modeReport : analysisResult.modeReports) {
        System.out.println(modeReport);
    }
    Plot.plot(sample, compositeDistribution::pdf, "Final");
```

### Using Custom Configuration
`StatAnalyzer` class has a builder, so you can configure it as needed: use a custom criteria, change parameters of multimodality detector, etc.

```java
    // put your sample here
    List<Double> dataList = new ArrayList<>(30000);
    
    Sample sample = new Sample(dataList);

    StatAnalyzer statAnalyzer = StatAnalyzer.builder()
            .useJittering(true)
            .recursiveModeDetection(true)
            .distributionTest(new CramerVonMises())
            .parameterEstimator(new CramerVonMises())
            .optimizeFinalSolution(true)
            .modeDetector(new LowlandModalityDetector(0.95, 0.05, false))
            .build();

    AnalysisResult analysisResult = statAnalyzer.analyze(dataList);

    PgCompositeDistribution compositeDistribution = analysisResult.compositeDistribution;

    for (var modeReport : analysisResult.modeReports) {
            System.out.println(modeReport.toStringVerbose());
    }
    Plot.plot(sample, compositeDistribution::pdf, "Final");
```

## Output Report Format
### Short format
For each found mode you will get a short report with only the best found distribution
```
=== Mode Report (Summary) ===
Mode bounds: [317348.11, 709199.13]
Location: 408170.596540
Mode size: 8442
Best Distribution:
  Type: LOGNORMAL
  Parameters: [13.060608722495797, 0.22158907133178077]
  p-value: 1.5765894150865378E-5
```
### Verbose format
For verbose format you can use method `toStringVerbose()` inside a `ModeReport` class
```System.out.println(modeReport.toStringVerbose());```

This way, you obtain not only the best-fitting distribution but also all distributions that StatAnalyzer attempted to fit
```
=== Mode Report ===
Mode bounds: [317348.11, 709199.13]
Location: 408170.596540
Mode size: 8440
Best Distribution:
  Type: LOGNORMAL
  Parameters: [13.06105666934902, 0.2211888362546083]
  p-value: 1.9891912495406672E-5

All Fitted Distributions:
  #1
  Type: LOGNORMAL
  Parameters: [13.06105666934902, 0.2211888362546083]
    p-value: 1.9891912495406672E-5
  #2
  Type: FRECHET
  Parameters: [5.380420892081199, 435822.8848008284]
    p-value: 5.34160733312028E-7
  #3
  Type: GUMBEL
  Parameters: [438947.0514468848, 77694.66790574412]
    p-value: 1.090679280224549E-8
  #4
  Type: WEIBULL
  Parameters: [5.018470419785777, 518538.7200727775]
    p-value: 2.3060442444489127E-12
```

## Supported Distributions
 - Uniform
 - Normal
 - Lognormal
 - Frechet
 - Gumbel
 - Gamma
 - Weibull

## Supported criterias
 - Kolmogorov-Smirnov
 - Cramer-von-Mises
 - Maximum Likelihood
 - Multicriteria
 - Pearson

## Installation

### Maven
Add another dependency to your pom.xml
```xml
<dependency>
    <groupId>ru.postgrespro.perf.pgmicrobench</groupId>
    <artifactId>pg-statanalyzer</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

## Documentation
Is published on GitHub Pages 

https://pg-statanalyzer.github.io/pg-statanalyzer/

## Contributing
If you want to contribute — feel free to submit pull requests. 
### How to add a new distribution
 - You must create a new class inside a `ru.postgrespro.perf.statanalyzer.distributions` package, which should implement the `PgSimpleDistribution` interface
 - Implement all the methods, such as pdf, cdf, mean, variance etc.
 - For methods such as `generate(int size, Random random)` use already implemented distributions as a reference.
 - You are all done!

```java
public class PgMyDistribution implements PgSimpleDistribution {
    @Override
    public double mean() {...}

    @Override
    public double variance() {...}

    @Override
    public double median() {...}

    @Override
    public double skewness() {...}
    ...
}
```
## License
This project is licensed under the MIT License.
## Authors
Rustam Khamidullin

Daria Barsukova

Evgeniy Buzyurkin