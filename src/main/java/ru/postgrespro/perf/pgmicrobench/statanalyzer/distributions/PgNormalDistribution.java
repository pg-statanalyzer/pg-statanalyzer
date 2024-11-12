package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

public class PgNormalDistribution implements PgDistribution {
    @Override
    public int getParameterNumber() {
        return 2;
    }

    @Override
    public PgDistributionSample getSample(double[] params) {
        return new PgNormalDistributionSample(params[0], params[1]);
    }
}
