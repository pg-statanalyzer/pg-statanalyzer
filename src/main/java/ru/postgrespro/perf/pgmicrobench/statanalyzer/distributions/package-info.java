/**
 * Probability distribution implementations and fitting utilities.
 *
 * <p>This package contains implementations of statistical probability distributions
 * that can be fitted to data samples. Each distribution implements the
 * {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution} interface and provides methods
 * for probability density function (PDF), cumulative distribution function (CDF),
 * and statistical moments.</p>
 *
 * <p>Supported distributions include:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgNormalDistribution} - Normal/Gaussian distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgLogNormalDistribution} - Log-normal distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgGammaDistribution} - Gamma distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgWeibullDistribution} - Weibull distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgFrechetDistribution} - Frechet (Type II extreme value) distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgGumbelDistribution} - Gumbel (Type I extreme value) distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgUniformDistribution} - Uniform distribution</li>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgCompositeDistribution} - Composite of multiple distributions for multimodal data</li>
 * </ul>
 *
 * <p>Distribution types are defined in {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType}.</p>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistributionType
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition for distribution fitting algorithms
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;