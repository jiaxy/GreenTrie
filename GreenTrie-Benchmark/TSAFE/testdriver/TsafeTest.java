package testdriver;


import static org.junit.Assert.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import util.SPFLaunchor;

public class TsafeTest {

	@Test
	public void testDriver_RT_S_1() {
		String target="testdriver.Driver_RT_S_1";
		String method="testdriver.Driver_RT_S_1.RT_S_1(sym#sym#sym#sym)";
		SPFLaunchor.runSPFWithoutCache(target, method, "coral");
	}

}
