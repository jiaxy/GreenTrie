import static org.junit.Assert.*;

import org.junit.Test;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import util.SPFLaunchor;

public class TSafeTest {

	@Test
	public void test() {
		
		//fail("Not yet implemented");
		 Config conf = JPF.createConfig(SPFLaunchor.JPF_ARGS_NOCACHE);
		 conf.setProperty("classpath", "bin;lib/feed_server.jar;lib/figio.jar");
		 conf.setProperty("target", "testdriver.Driver_RT_S_1");
		 conf.setProperty("symbolic.lazy", "true");
		 //conf.setProperty("target", "testdriver.Driver_RT_S_1");
		 
		 conf.setProperty("symbolic.method","testdriver.Driver_RT_S_1.RT_S_1(sym#sym#sym#sym)");
		 
		 
		 JPF jpf = new JPF(conf);
		 jpf.addListener(new DebugListener(conf, jpf));
		 jpf.run();
//		
		
	}

}
