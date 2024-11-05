package util.parsing;

import org.apache.commons.math3.distribution.RealDistribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * The DistributionParser class provides functionality to parse distribution data
 * from a specified file and create corresponding distribution objects.
 */
public class DistributionParser {
	private static final String DIRECTORY = "distributionSample/";

	/**
	 * Parses a distribution file and creates an array of RealDistribution objects along
	 * with the associated data.
	 *
	 * @param fileName the name of the file containing the distribution data.
	 * @return a ParsedDistribution object containing the parsed distributions and data.
	 * @throws FileNotFoundException if the specified file does not exist.
	 */
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
