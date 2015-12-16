package probsym;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import za.ac.sun.cs.green.EntireSuite;
import za.ac.sun.cs.green.GreenListener;
import za.ac.sun.cs.green.service.SATService;
import za.ac.sun.cs.green.util.Reporter;

public class TestUtil {

	public static void initTries(Config conf) {
		conf.setProperty("symbolic.green","true");
		conf.setProperty("green.services",
				"sat");
		conf.setProperty("green.service.sat",
				"(slice (canonize coral))");
		conf.setProperty("green.store",
				"cn.edu.whu.sklse.greentrie.store.TrieStore");
		conf.setProperty("green.service.sat.slice",
				"cn.edu.whu.sklse.greentrie.slice.SATSlicerService");
		conf.setProperty("green.service.sat.canonize",
				"cn.edu.whu.sklse.greentrie.canolize.SATCanonizerService");
		conf.setProperty("green.service.sat.z3",
				"cn.edu.whu.sklse.greentrie.z3.SATZ3JavaService");
		conf.setProperty("green.service.sat.coral",
				"cn.edu.whu.sklse.greentrie.coral.SATCoralService");
		conf.setProperty("green.service.sat.iasolver",
				"cn.edu.whu.sklse.greentrie.iasolver.SATIASolverService");
		
		
		conf.setProperty("green.z3.path", "/Users/jiaxy/Dropbox/z3/z3-x64-osx-10.9.5/bin/z3");
		// clearTrie(conf);
	}

	public static void initUBTrees(Config conf) {
		conf.setProperty("green.store",
				"cn.edu.whu.sklse.ubtree.UBTreeStore");
		conf.setProperty("green.service.sat.canonize",
				"cn.edu.whu.sklse.ubtree.SATCanonizerService");
		// clearTrie(conf);
	}

	public static void clearTrie(Config conf) {
		String basePath = conf.getString("constraint.store.basePath");
		File f1 = new File(basePath + "/satisfiableTrie");
		f1.deleteOnExit();
		File f2 = new File(basePath + "/unsatisfiableTrie");
		f2.deleteOnExit();
	}

	public static void clearUBTrees(Config conf) {
		String basePath = conf.getString("constraint.store.basePath");
		File f1 = new File(basePath + "/satisfiableTree");
		f1.deleteOnExit();
		File f2 = new File(basePath + "/unsatisfiableTree");
		f2.deleteOnExit();
	}

	public static void initRedis(Config conf) {
		conf.setProperty("green.store",
				"za.ac.sun.cs.green.store.redis.RedisStore");
		conf.setProperty("green.service.sat.canonize",
				"za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		// clearRedis();
		// jedis.shutdown();
	}

	public static void clearRedis() {
		JedisPoolConfig config = new JedisPoolConfig();
		JedisPool pool = new JedisPool(config, "127.0.0.1", 6379, 610 * 1000,
				null);
		Jedis jedis = pool.getResource();
		pool.returnResource(jedis);
		jedis.flushAll();
	}

	public static void testIncremental(Config conf, String[] classses,
			String[] methods) {
		List<String> result = new ArrayList<String>();
		for (String c : classses) {
			conf.put("target", c);
			String meth = "";
			for (String m : methods) {
				meth += c + "." + m + ";";
			}
			conf.put("symbolic.method", meth);
			String r = "result of " + meth + ":";
			r += "\n" + runJPF(conf);
			result.add(r);
		}
		for (String s : result) {
			System.out.println(s);
		}

	}

	public static String testAcrossProgram(Config target, Config pre,
			boolean isTrie) {
		if (isTrie) {
			initTries(pre);
			runJPF(pre);
			// initTries(target);
			target.setProperty("green.store",
					"gov.nasa.jpf.symbc.green.trie.TrieStore");
			target.setProperty("green.service.sat.canonize",
					"gov.nasa.jpf.symbc.green.trie.SATCanonizerService");
			return "resue trie store of class " + pre.getString("target") + ":"
					+ runJPF(target);
		} else {
			initRedis(pre);
			runJPF(pre);
			target.setProperty("green.store",
					"za.ac.sun.cs.green.store.redis.RedisStore");
			target.setProperty("green.service.sat.canonize",
					"za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
			return "resue redis store of class " + pre.getString("target")
					+ ":" + runJPF(target);
		}
	}

	public static String runJPF(Config conf) {
		final List<String> reportResult = new ArrayList<String>();
		JPF jpf = new JPF(conf);
		jpf.addListener(new GreenListener());
		long t1 = System.currentTimeMillis();
		jpf.run();
		reportResult.add("elapsed time=" + (System.currentTimeMillis() - t1));
		if(SymbolicInstructionFactory.greenSolver!=null){
			SymbolicInstructionFactory.greenSolver.report(new Reporter() {
				@Override
				public void report(String context, String message) {
					if (context.equals("SATCVC3Service")
							|| context.equals("SATZ3JavaService")) {
						reportResult.add(message);
					}
				}
			});
			
		}
		return reportResult.toString();
	}

	public static String[] reverse(String[] addClasses) {
		int length = addClasses.length;
		String[] deleteClasses = new String[length];
		for (int i = 0; i < length; i++) {
			deleteClasses[length - 1 - i] = addClasses[i];
		}
		return deleteClasses;
	}

	public static void getTestReport() {
		SATService s = (SATService) SymbolicInstructionFactory.greenSolver
				.getService("green.service.sat");

	}

}
