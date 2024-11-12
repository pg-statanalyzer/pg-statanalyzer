package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

public class PgLogNormalDistribution implements PgDistribution {
    @Override
    public int getParameterNumber() {
        return 2;
    }

    @Override
    public PgDistributionSample getSample(double[] params) {
        return new PgLogNormalDistributionSample(params[0], params[1]);
    }
}
