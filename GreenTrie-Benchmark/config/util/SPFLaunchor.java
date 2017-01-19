package util;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.GreenListener;

public class SPFLaunchor {

	private static Config buildCommonConfig(String target, String method) {
		Config conf = JPF.createConfig(new String[] { "+site=config/site.properties", "config/common.jpf" });
		conf.setTarget(target);
		conf.setProperty("symbolic.method", method);
		conf.setProperty("listener", ".symbc.SymbolicListener");
		conf.setProperty("symbolic.lazy", "true");
		conf.setProperty("coral.iterations", "2000");
		return conf;
	}

	private static void addNoCacheConfig(Config conf, String solver) {
		conf.setProperty("symbolic.dp", solver);
	}

	private static void addGreenConfig(Config conf, String solver) {
		conf.setProperty("symbolic.green", "true");
		conf.setProperty("green.taskmanager", "za.ac.sun.cs.green.taskmanager.ParallelTaskManager");
		conf.setProperty("green.store", "za.ac.sun.cs.green.store.redis.RedisStore");
		conf.setProperty("green.services", "sat");
		conf.setProperty("green.service.sat", "(slice (canonize " + solver + "))");
		conf.setProperty("green.service.sat.slice", "za.ac.sun.cs.green.service.slicer.SATSlicerService");
		conf.setProperty("green.service.sat.canonize", "za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		if (solver.equals("z3")) {
			conf.setProperty("green.service.sat.z3", "za.ac.sun.cs.green.service.z3.SATZ3JavaService");
		} else if (solver.equals("choco")) {
			conf.setProperty("green.service.sat.choco", "za.ac.sun.cs.green.service.choco.SATChocoService");
		}
		conf.setProperty("listener", ".symbc.GreenListener");
	}

	private static void addGreenTrieConfig(Config conf, String solver) {
		conf.setProperty("symbolic.green", "true");
		conf.setProperty("green.taskmanager", "za.ac.sun.cs.green.taskmanager.ParallelTaskManager");
		conf.setProperty("green.store", "cn.edu.whu.sklse.greentrie.store.TrieStore");
		conf.setProperty("green.services", "sat");
		conf.setProperty("green.service.sat", "(slice (canonize " + solver + "))");
		conf.setProperty("green.service.sat.slice", "cn.edu.whu.sklse.greentrie.slice.SATSlicerService");
		conf.setProperty("green.service.sat.canonize", "cn.edu.whu.sklse.greentrie.canolize.SATCanonizerService");
		if (solver.equals("z3")) {
			conf.setProperty("green.service.sat.z3", "cn.edu.whu.sklse.greentrie.z3.SATZ3JavaService");
		} else if (solver.equals("choco")) {
			conf.setProperty("green.service.sat.choco", "cn.edu.whu.sklse.greentrie.choco.SATChocoService");
		} else if (solver.equals("coral")) {
			conf.setProperty("green.service.sat.coral", "cn.edu.whu.sklse.greentrie.coral.SATCoralService");
		}
		
	}

	public static void runSPFWithoutCache(String target, String method, String solver) {
		Config conf = buildCommonConfig(target,method);
		addNoCacheConfig(conf, solver);
		JPF jpf = new JPF(conf);
		jpf.run();
	}

	public static void runSPFWithGreen(String target, String method, String solver) {
		Config conf = buildCommonConfig(target,method);
		addGreenConfig(conf, solver);
		JPF jpf = new JPF(conf);
		jpf.run();
	}
	
	public static void runSPFWithGreenTrie(String target, String method, String solver) {
		Config conf = buildCommonConfig(target,method);
		addGreenTrieConfig(conf, solver);
		JPF jpf = new JPF(conf);
		jpf.addListener(new GreenListener());
		jpf.run();
	}

}
