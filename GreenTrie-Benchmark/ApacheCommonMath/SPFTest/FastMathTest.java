package SPFTest;

import static org.junit.Assert.*;

import org.junit.Test;

import util.SPFLaunchor;

public class FastMathTest {

	@Test
	public void test() {
		String target="SPFTest.FastMathCosh";
		String method="SPFTest.FastMathCosh.cosh(sym)";
		SPFLaunchor.runSPFWithoutCache(target, method, "coral");
	}
	
}
