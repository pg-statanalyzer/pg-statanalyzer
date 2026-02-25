/**
 * Density histogram implementations for probability density estimation.
 *
 * <p>This package provides specialized histogram implementations designed for
 * probability density estimation, particularly useful for multimodality detection
 * and distribution fitting.</p>
 *
 * <p>Key classes:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram} - Basic density histogram</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogramBin} - Individual bin in a density histogram</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.IDensityHistogramBuilder} - Interface for building density histograms</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.QuantileRespectfulDensityHistogramBuilder} - Builder that respects quantile boundaries</li>
 * </ul>
 *
 * <p>Density histograms differ from regular histograms by:</p>
 * <ul>
 *   <li>Normalizing bin heights to represent probability density (area under histogram = 1)</li>
 *   <li>Providing better support for probability distribution comparisons</li>
 *   <li>Enabling accurate multimodality detection through density estimation</li>
 * </ul>
 *
 * <p>For general histogram utilities, see the parent package {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram}.</p>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.DensityHistogram
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density.IDensityHistogramBuilder
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram.density;