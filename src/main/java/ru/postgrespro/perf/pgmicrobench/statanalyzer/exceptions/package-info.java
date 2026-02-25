/**
 * Custom exceptions for the statistical analysis library.
 *
 * <p>This package contains exception classes that represent specific error
 * conditions that may occur during statistical analysis.</p>
 *
 * <p>Available exceptions:</p>
 * <ul>
 *   <li>{@link ru.postgrespro.perf.pgmicrobench.statanalyzer.exceptions.WeightedSampleNotSupportedException} -
 *       Thrown when an operation does not support weighted samples</li>
 * </ul>
 *
 * <p>All exceptions in this package extend {@link java.lang.RuntimeException}
 * and are unchecked, allowing for cleaner client code while still providing
 * meaningful error information.</p>
 *
 * @see ru.postgrespro.perf.pgmicrobench.statanalyzer.exceptions.WeightedSampleNotSupportedException
 */
package ru.postgrespro.perf.pgmicrobench.statanalyzer.exceptions;