package org.apache.commons.math4;

import util.SPFLaunchor;

public class DescriptiveStatisticsTest {

	public static void main(String[] args) {
		String target="org.apache.commons.math4.DescriptiveStatisticsDriver";
		String method="org.apache.commons.math4.DescriptiveStatisticsDriver.getDeviation(sym)";
//		
		
//		String target="StrictMathExample";
//		String method="StrictMathExample.computeTan2(sym)";
		
		
		SPFLaunchor.runSPF(target, method, SPFLaunchor.JPF_ARGS_NOCACHE_CORAL);

	}

}
