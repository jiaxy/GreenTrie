package SPFTest;

import static org.junit.Assert.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import util.SPFLaunchor;

public class DescriptiveStatisticsTest {

	@Test
	public void test() {
		String target="SPFTest.DescriptiveStatisticsTestDriver";
		String method="SPFTest.DescriptiveStatisticsTestDriver.test(sym)";
		SPFLaunchor.runSPFWithoutCache(target, method, "coral");
	}

}


class DescriptiveStatisticsTestDriver{
	
	public static void main(String[] args) {
		test(new double[4]);
	}
	
	public static double test(double[] inputArray){
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics stats = new DescriptiveStatistics();

		// Add the data from the array
		for( int i = 0; i < inputArray.length; i++) {
		        stats.addValue(inputArray[i]);
		}

		// Compute some statistics
		//double mean = stats.getMean();
		double std = stats.getStandardDeviation();
		double median = stats.getPercentile(50);
		return std;
	}
}
