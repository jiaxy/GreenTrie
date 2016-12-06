package org.apache.commons.math4;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DescriptiveStatisticsDriver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics stats = new DescriptiveStatistics();

		double[] inputArray =new double[]{10,20,24,103.2};
		// Add the data from the array
		for( int i = 0; i < inputArray.length; i++) {
		        stats.addValue(inputArray[i]);
		}

		// Compute some statistics
		double mean = stats.getMean();
		double std = getDeviation(inputArray);
		double median = stats.getPercentile(50);
		//System.out.println("mean:"+mean+" std:"+std+" median:"+median);
	}

	private static double getDeviation(double[] inputArray) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for( int i = 0; i < inputArray.length; i++) {
	        stats.addValue(inputArray[i]);
	}
		return stats.getStandardDeviation();
	}
	
	
	
	

}
