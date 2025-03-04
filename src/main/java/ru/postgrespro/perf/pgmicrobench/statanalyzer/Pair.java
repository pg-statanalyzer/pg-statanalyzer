package ru.postgrespro.perf.pgmicrobench.statanalyzer;

/**
 * Class pair.
 *
 * @param <A> type.
 */
public class Pair<A> {
    public final A first;
    public final A second;

    public Pair(A first, A second) {
        this.first = first;
        this.second = second;
    }
}
