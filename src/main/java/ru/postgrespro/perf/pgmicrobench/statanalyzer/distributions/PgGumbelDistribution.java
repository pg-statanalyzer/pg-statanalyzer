package ru.postgrespro.perf.pgmicrobench.statanalyzer.distributions;

public class PgGumbelDistribution implements PgDistribution{
    @Override
    public int getParameterNumber() {
        return 2;
    }

    @Override
    public PgDistributionSample getSample(double[] params) {
        return new PgGumbelDistributionSample(params[0], params[1]);
    }
}
