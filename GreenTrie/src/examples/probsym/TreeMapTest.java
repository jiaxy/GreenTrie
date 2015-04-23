package probsym;

import static org.junit.Assert.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;

public class TreeMapTest {
	//static int count=0;
	Config conf = JPF.createConfig(new String[]{});
	
	@Before
	public void setup() throws FileNotFoundException, IOException{
		conf.put("constraint.store.basePath", "store/constraint");
		conf.load(new FileInputStream(new File("src/examples/TreeMap.jpf")));
	}
	
	
//	@Test
//	public void test() {
////		count++;
////		System.out.println("Test!");
////		//ProcessHelper2.runJPF("test/Trityp");
////		saveObject("test","output_"+count,"TEST");
//		try {
//			Config conf = JPF.createConfig(new String[]{});
//			conf.put("constraint.store.basePath", "store/constraint");
//			conf.load(new FileInputStream(new File("test/TreeMap.jpf")));
//			JPF jpf = new JPF(conf);
//			jpf.run();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	@Test
	public void testTrieStore() {
		//conf.setProperty("green.store", "gov.nasa.jpf.symbc.green.trie.TrieStore");
		//conf.setProperty("green.service.sat.canonize", "gov.nasa.jpf.symbc.green.trie.SATCanonizerService");
		TestUtil.initTries(conf);
		TestUtil.runJPF(conf);
	}
	@Test
	public void testRedisStore() {
		//conf.setProperty("green.store", "za.ac.sun.cs.green.store.redis.RedisStore");
		//conf.setProperty("green.service.sat.canonize", "za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		TestUtil.initRedis(conf);
		TestUtil.runJPF(conf);
	}

	
	@Test
	public void testRedis() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.runJPF(conf);
	}
	
	@Test
	public void testUBTrees() {
		TestUtil.initUBTrees(conf);
		TestUtil.clearUBTrees(conf);
		TestUtil.runJPF(conf);
	}
	
	@Test
	public void testTries() {
		TestUtil.initTries(conf);
		TestUtil.clearTrie(conf);
		TestUtil.runJPF(conf);
	}
	

	
}
