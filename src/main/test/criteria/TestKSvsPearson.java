package criteria;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.junit.jupiter.api.Test;
import ru.postgrespro.perf.pgmicrobench.statanalyzer.criteria.Pearson;
import util.parsing.DistributionParser;
import util.parsing.ParsedDistribution;

import java.io.FileNotFoundException;

public class TestKSvsPearson {
	@Test
	public void testKSvsPearson() throws FileNotFoundException {
		String[] files = new String[] {
				"lognorm_13_07",
				"lognorm_4278_001",
				"lognorm_3_0.3",
				"gamma_10_03"
		};


		for (String file : files) {
			ParsedDistribution parsedDistribution = DistributionParser.parseDistribution(file);

			double[] data = parsedDistribution.getData();
			RealDistribution distribution = parsedDistribution.getDistribution()[0];

			KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();

			double pValue = test.kolmogorovSmirnovTest(distribution, data);
			double pearsonPValue = Pearson.chiSquareTest(data, distribution, 2, 50);

			System.out.println(file + " " + (pValue + " " + pearsonPValue).replace('.', ','));


			System.out.println();
		}
	}
}
