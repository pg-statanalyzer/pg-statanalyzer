package util.parsing;

import org.apache.commons.math3.distribution.RealDistribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DistributionParser {
	private static final String DIRECTORY = "distributionSample/";

	public static ParsedDistribution parseDistribution(String fileName) throws FileNotFoundException {
		String path = DIRECTORY + fileName;
		int totalCount = 0;

		try (Scanner scanner = new Scanner(new File(path))) {
			int distributionCount = scanner.nextInt();
			RealDistribution[] distributions = new RealDistribution[distributionCount];

			for (int j = 0; j < distributionCount; j++) {
				String distributionName = scanner.next();

				DistributionType distributionType = DistributionType.valueOf(distributionName.toUpperCase());
				int parameterCount = distributionType.getParameters();

				double[] params = new double[parameterCount];
				for (int i = 0; i < parameterCount; i++) {
					params[i] = scanner.nextDouble();
				}

				int size = scanner.nextInt();
				totalCount += size;

				distributions[j] = distributionType.createDistribution(params);
			}

			double[] data = new double[totalCount];

			for (int i = 0; i < totalCount; i++) {
				data[i] = scanner.nextDouble();
			}

			return new ParsedDistribution(distributions, data);
		}
	}
}
