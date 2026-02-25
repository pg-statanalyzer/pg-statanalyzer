/**
 * Distribution fitting, parameter estimation, and goodness-of-fit testing.
 *
 * <p>This package provides algorithms for:</p>
 * <ol>
 *   <li>Estimating distribution parameters from sample data using various estimators</li>
 *   <li>Testing how well a distribution fits the data using statistical tests</li>
 *   <li>Selecting the best-fitting distribution based on multiple criteria</li>
 * </ol>
 *
 * <p>Key interfaces and classes:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IParameterEstimator} - Interface for parameter estimation algorithms</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IDistributionTest} - Interface for goodness-of-fit tests</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.FittedDistribution} - Result of fitting a distribution to data</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.EstimatedParameters} - Parameter estimates with confidence intervals</li>
 * </ul>
 *
 * <p>Supported statistical tests:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.KolmogorovSmirnov} - Kolmogorov-Smirnov test</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.CramerVonMises} - Cramér–von Mises test</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Pearson} - Pearson's chi-squared test</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.MaximumLikelihoodEstimation} - MLE with likelihood ratio test</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.Multicriteria} - Combined multiple criteria approach</li>
 * </ul>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IParameterEstimator
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition.IDistributionTest
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;