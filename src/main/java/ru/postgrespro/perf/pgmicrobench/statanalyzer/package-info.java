/**
 * Core statistical analysis engine for benchmark and performance data.
 *
 * <p>The main entry point is {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.StatAnalyzer} which provides
 * a builder pattern for configuring analysis and the main {@code analyze} method
 * for processing samples.</p>
 *
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Multimodality detection using lowland algorithms</li>
 *   <li>Statistical distribution fitting and parameter estimation</li>
 *   <li>Goodness-of-fit testing with multiple criteria</li>
 *   <li>Sample preprocessing including jittering</li>
 *   <li>Result visualization and reporting</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * StatAnalyzer analyzer = StatAnalyzer.builder()
 *     .useJittering(true)
 *     .distributionTest(new CramerVonMises())
 *     .build();
 * AnalysisResult result = analyzer.analyze(data);
 * }</pre>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.StatAnalyzer
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.AnalysisResult
 * @see <a href="https://pg-statanalyzer.github.io/pg-statanalyzer/">Documentation</a>
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer;