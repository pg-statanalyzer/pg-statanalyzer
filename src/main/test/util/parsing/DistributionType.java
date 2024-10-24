package util.parsing;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import java.util.function.Function;

public enum DistributionType {
	LOGNORM(2, (params -> new LogNormalDistribution(params[0], params[1]))),
	GAMMA(2, (params -> new GammaDistribution(params[0], params[1]))),
	;

	private final int parameters;
	private final Function<double[], RealDistribution> createDistributionFunction;

	DistributionType(int parameters, Function<double[], RealDistribution> createDistributionFunction) {
		this.parameters = parameters;
		this.createDistributionFunction = createDistributionFunction;
	}

	public int getParameters() {
		return parameters;
	}

	public RealDistribution createDistribution(double[] params) {
		return createDistributionFunction.apply(params);
	}
}
