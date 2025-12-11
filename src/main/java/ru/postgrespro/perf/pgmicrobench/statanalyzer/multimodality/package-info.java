/**
 * Multimodality detection and mode analysis algorithms.
 *
 * <p>This package implements algorithms for detecting multiple modes (peaks) in
 * statistical distributions. The primary algorithm is based on the
 * <a href="https://aakinshin.net/posts/lowland-multimodality-detection/">
 * Lowland Multimodality Detection</a> approach by Andrey Akinshin.</p>
 *
 * <p>Key components:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector} - Basic lowland algorithm</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RecursiveLowlandModalityDetector} - Recursive detection for complex multimodal data</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.ModalityData} - Results of modality detection</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RangedMode} - Representation of a detected mode with bounds</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.DiagnosticsBin} - Helper for histogram-based analysis</li>
 * </ul>
 *
 * <p>The algorithm works by:</p>
 * <ol>
 *   <li>Creating a density histogram of the sample</li>
 *   <li>Identifying "lowlands" (valleys) between peaks</li>
 *   <li>Applying threshold-based filtering to distinguish significant modes from noise</li>
 *   <li>Splitting the sample into unimodal subsets for further analysis</li>
 * </ol>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.LowlandModalityDetector
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality.RecursiveLowlandModalityDetector
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.multimodality;