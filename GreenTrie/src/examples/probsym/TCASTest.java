package probsym;

import static org.junit.Assert.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import za.ac.sun.cs.green.expr.VisitorException;
import cvc3.Expr;
import cvc3.ExprMut;
import cvc3.FlagsMut;
import cvc3.SatResult;
import cvc3.Type;
import cvc3.ValidityChecker;

public class TCASTest {
	Config conf = JPF.createConfig(new String[] {});
	String[] addClasses = new String[] { "probsym.TCAS", "probsym.add.TCAS1",
			"probsym.add.TCAS2", "probsym.add.TCAS3" };
	String[] modifyClasses = new String[] { "probsym.TCAS",
			"probsym.modify.TCAS1", "probsym.modify.TCAS2",
			"probsym.modify.TCAS3" };
	String[] methods = new String[] {
			"startTcas(sym#sym#sym#sym#sym#sym#sym#sym#sym#sym#sym#sym)",
			"alt_sep_test()" };

	@Before
	public void setup() throws FileNotFoundException, IOException {
		conf.put("constraint.store.basePath", "store/constraint");
		conf.load(new FileInputStream(new File("src/examples/TCAS.jpf")));
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
		TestUtil.testIncremental(conf, addClasses, methods);
	}

	@Test
	public void testAddRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.testIncremental(conf, addClasses, methods);
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
		String[] deleteClasses = TestUtil.reverse(addClasses);
		TestUtil.testIncremental(conf, deleteClasses, methods);
	}

	@Test
	public void testDeleteRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		String[] deleteClasses = TestUtil.reverse(addClasses);
		TestUtil.testIncremental(conf, deleteClasses, methods);
	}

	@Test
	public void testMofifyTrieStore() {
		TestUtil.initTries(conf);
		TestUtil.clearTrie(conf);
		TestUtil.testIncremental(conf, modifyClasses, methods);
	}

	@Test
	public void testMofifyRedisStore() {
		TestUtil.initRedis(conf);
		TestUtil.clearRedis();
		TestUtil.testIncremental(conf, modifyClasses, methods);
	}

}
