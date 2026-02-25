package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.recognition;

import ru.postgrespro.perf.pgmicrobench.statanalyzer.sample.Sample;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions.PgDistribution;

public interface IDistributionTest {
    /**
     * Calculate statistic of a distribution test, some value which describes how similar
     * your sample is to a presumed distribution. Depending on criteria, might be lower-better
     * or higher-better, so it's better to rely on p-value,
     * which can be calculated by {@link #test(Sample, PgDistribution)}.
     *
     * @param sample Sample
     * @param distribution Presumed distribution
     * @return double statistic
     */
    double statistic(Sample sample, PgDistribution distribution);

    /**
     * Calculate p-value of distribution test, which describes how likely your sample has presumed distribution type.
     * If your p-value is less than 0.05 (alpha), then your sample doesn't have this distribution type.
     * If your p-value is more than 0.05, then MAYBE your sample has this distribution type. It is kind of tricky,
     * because you can't tell for certain that your sample has this distribution type, but you can't deny it either.
     * We recommend to do it this way: if out of list of distributions only one has p-value > 0.05, then you have your
     * best distribution. If 2 or more distributions have p-value > 0.05, then it's on user's choice.
     *
     * @param sample Sample
     * @param distribution Presumed distribution
     * @return p-value
     */
    double test(Sample sample, PgDistribution distribution);
}
