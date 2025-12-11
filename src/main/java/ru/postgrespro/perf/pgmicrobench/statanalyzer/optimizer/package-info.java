/**
 * Numerical optimization algorithms for parameter estimation.
 *
 * <p>This package contains optimization algorithms used to find the best-fitting
 * parameters for probability distributions. The primary implementation is
 * {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer.PgOptimizer}, which uses numerical methods
 * to maximize goodness-of-fit criteria.</p>
 *
 * <p>Optimization goals:</p>
 * <ul>
 *   <li>Maximize p-value of goodness-of-fit tests</li>
 *   <li>Minimize distance between empirical and theoretical distributions</li>
 *   <li>Find maximum likelihood estimates for distribution parameters</li>
 * </ul>
 *
 * <p>Supported optimization methods:</p>
 * <ul>
 *   <li> Covariance Matrix Adaptation Evolution Strategy (CMA-ES)/li>
 * </ul>
 *
 * <p>The optimizer is used internally during distribution fitting to:</p>
 * <ol>
 *   <li>Find initial parameter estimates using method of moments or other heuristics</li>
 *   <li>Refine parameters by maximizing the p-value of the selected goodness-of-fit test</li>
 * </ol>
 *
 * <p>For distribution-specific parameter estimation algorithms, see
 * {@link ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition}.</p>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer.PgOptimizer
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.optimizer;