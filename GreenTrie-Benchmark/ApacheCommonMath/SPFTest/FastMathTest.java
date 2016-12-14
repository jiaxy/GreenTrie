package SPFTest;

import static org.junit.Assert.*;

import org.junit.Test;

import util.SPFLaunchor;

public class FastMathTest {

	@Test
	public void test() {
		String target="SPFTest.FastMathCosh";
		String method="org.apache.commons.math3.util.FastMath.cosh(sym)";
		SPFLaunchor.runSPF(target, method, SPFLaunchor.JPF_ARGS_NOCACHE_CORAL);
	}

}
