package util;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class SPFLaunchor {

	 public static final String[] JPF_ARGS_NOCACHE = 
			new String[] {"+site=config/site.properties", "config/common.jpf"};
	 
	 public static final String[] JPF_ARGS_NOCACHE_CORAL = 
				new String[] {"+site=config/site.properties", "config/nocache_coral.jpf"};

	 public static final String[] JPF_ARGS_GREEN = 
			new String[] {"+site=config/site.properties", "config/common.jpf", "config/green.jpf"};
	
	 public static final String[] JPF_ARGS_GREENTRIE = 
			new String[] {"+site=config/site.properties", "config/common.jpf", "config/greentrie.jpf"};
	 
	 
	 
	 public static void runSPF(String target,String method,String[] baseConf){
		 Config conf = JPF.createConfig(baseConf);
		 conf.setProperty("target", target);
		 conf.setProperty("symbolic.method",method);
		 JPF jpf = new JPF(conf);
		 jpf.run();
	 }
	
	 
}
