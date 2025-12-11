/**
 * Statistical estimators for quantiles and other sample statistics.
 *
 * <p>This package provides specialized estimators for statistical quantities,
 * particularly focused on robust quantile estimation.</p>
 *
 * <p>Key components:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.IQuantileEstimator} - Interface for quantile estimation algorithms</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.HarrellDavisQuantileEstimator} - Harrell-Davis quantile estimator</li>
 * </ul>
 *
 * <p>The Harrell-Davis estimator provides:</p>
 * <ul>
 *   <li>Reduced variance compared to simple sample quantiles</li>
 *   <li>Better statistical properties for small to moderate sample sizes</li>
 *   <li>Smooth estimates across the quantile range</li>
 *   <li>Appropriate confidence intervals for quantile estimates</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * IQuantileEstimator estimator = new HarrellDavisQuantileEstimator();
 * double median = estimator.estimate(sample, 0.5);
 * double q90 = estimator.estimate(sample, 0.9);
 * }</pre>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.IQuantileEstimator
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators.HarrellDavisQuantileEstimator
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators;