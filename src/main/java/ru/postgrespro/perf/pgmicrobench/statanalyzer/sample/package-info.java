/**
 * Sample representations and statistical computations.
 *
 * <p>This package provides classes for representing statistical samples with
 * efficient computation of common statistical metrics. The {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample}
 * class is the primary representation for univariate data samples.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Lazy computation of statistical metrics (mean, variance, quantiles, etc.)</li>
 *   <li>Support for both unweighted and weighted samples</li>
 *   <li>Immutable design for thread safety</li>
 *   <li>Integration with other components of the library</li>
 * </ul>
 *
 * <p>Available classes:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample} - Main class for unweighted samples</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.WeightedSample} - Extension supporting weighted observations</li>
 * </ul>
 *
 * <p>Computed statistics include:</p>
 * <ul>
 *   <li>Basic: mean, median, variance, standard deviation</li>
 *   <li>Order statistics: minimum, maximum, quantiles</li>
 *   <li>Shape: skewness, kurtosis</li>
 *   <li>Robust statistics: median absolute deviation (MAD)</li>
 * </ul>
 *
 * <p>Note: For quantile estimation with specialized algorithms, see the
 * {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.estimators} package.</p>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.WeightedSample
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.sample;