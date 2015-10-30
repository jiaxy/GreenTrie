package probsym;

import static org.junit.Assert.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whu.sklse.greentrie.store.FileUtil;
import cn.edu.whu.sklse.greentrie.store.Trie;

public class BinTreeTest {
	
	Config conf = JPF.createConfig(new String[]{});
	String[] addClasses=new String[]{"probsym.BinTree","probsym.add.BinTree1","probsym.add.BinTree2","probsym.add.BinTree3"};
	String[] modifyClasses=new String[]{"probsym.BinTree","probsym.modify.BinTree1","probsym.modify.BinTree2","probsym.modify.BinTree3"};
	String[] methods=new String[]{"add(sym)"};

	@Before
	public void setup() throws FileNotFoundException, IOException{
		conf.put("constraint.store.basePath", "store/constraint");
		conf.load(new FileInputStream(new File("src/examples/BinTree.jpf")));
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
	public void testAddRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.testIncremental(conf,addClasses,methods);
	}
	
	@Test
	public void testAddUBTreeStore() {
		TestUtil.initUBTrees(conf);
		TestUtil.clearUBTrees(conf);
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
	public void testDeleteUBTreeStore() {
		TestUtil.initUBTrees(conf);
		TestUtil.clearUBTrees(conf);
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
	
	@Test
	public void testMofifyUBTreeStore() {
		TestUtil.initUBTrees(conf);
		TestUtil.clearUBTrees(conf);
		TestUtil.testIncremental(conf,modifyClasses,methods);
	}
	

}
