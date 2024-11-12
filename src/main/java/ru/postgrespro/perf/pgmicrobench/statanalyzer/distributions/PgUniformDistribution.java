package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

public class PgUniformDistribution implements PgDistribution {
    @Override
    public int getParameterNumber() {
        return 2;
    }

    @Override
    public PgDistributionSample getSample(double[] params) {
        return new PgUniformDistributionSample(params[0], params[1]);
    }
}
