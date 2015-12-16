package rjc;



import static org.junit.Assert.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import probsym.TestUtil;

public class RjcTest {
	Config conf = JPF.createConfig(new String[]{});
	String[] addClasses=new String[]{"probsym.BinomialHeap"
			,"probsym.add.BinomialHeap1","probsym.add.BinomialHeap2","probsym.add.BinomialHeap3"};
	String[] modifyClasses=new String[]{"probsym.BinomialHeap"
			,"probsym.modify.BinomialHeap1","probsym.modify.BinomialHeap2","probsym.modify.BinomialHeap3"};
	String[] methods=new String[]{"insert(sym)","delete(sym)"};

	@Before
	public void setup() throws FileNotFoundException, IOException{
		//conf.put("classpath", "${Greentrie}/build");
//		conf.put("target", "rjc.RJCMain");
//		conf.put("symbolic.method", "rjc.rjc.MainSymbolic(sym#sym#sym#sym#sym#sym#con#con)");
		conf.put("constraint.store.basePath", "store/constraint");
		conf.load(new FileInputStream(new File("src/examples/rjc/rjc.jpf")));
		conf.setProperty("green.service.sat","(slice (canonize coral))");
		conf.setProperty("green","true");
		conf.setProperty("symbolic.dp","coral");
		//symbolic.dp=coral
	}
	
	@Test
	public void testRedis() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.runJPF(conf);
	}
	
	@Test
	public void testTries() {
		TestUtil.initTries(conf);
		TestUtil.clearTrie(conf);
		TestUtil.runJPF(conf);
	}
	
	@Test
	public void testUBTrees() {
		TestUtil.initUBTrees(conf);
		TestUtil.clearUBTrees(conf);
		TestUtil.runJPF(conf);
	}
	

	@Test
	public void testAddTrieStore() {
		TestUtil.initTries(conf);
		TestUtil.clearTrie(conf);
		TestUtil.testIncremental(conf,addClasses,methods);
	}
	
	@Test
	public void testAddUBTreeStore() {
		TestUtil.initUBTrees(conf);
		TestUtil.clearUBTrees(conf);
		TestUtil.testIncremental(conf,addClasses,methods);
	}

	@Test
	public void testAddRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.testIncremental(conf,addClasses,methods);
	}
	
	@Test
	public void testDeleteTrieStore() {
		TestUtil.initTries(conf);
		TestUtil.clearTrie(conf);
		String[] deleteClasses =TestUtil. reverse(addClasses);
		TestUtil.testIncremental(conf,deleteClasses,methods);
	}
	
	@Test
	public void testDeleteRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		String[] deleteClasses =TestUtil. reverse(addClasses);
		TestUtil.testIncremental(conf,deleteClasses,methods);
	}
	
	@Test
	public void testMofifyTrieStore() {
		TestUtil.initTries(conf);
		TestUtil.clearTrie(conf);
		TestUtil.testIncremental(conf,modifyClasses,methods);
	}
	@Test
	public void testMofifyRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.testIncremental(conf,modifyClasses,methods);
	}

}
