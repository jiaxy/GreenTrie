package probsym;

import static org.junit.Assert.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class TritypTest {
	Config conf = JPF.createConfig(new String[]{});
	String[] addClasses=new String[]{"probsym.Trityp"
			,"probsym.add.Trityp1","probsym.add.Trityp2","probsym.add.Trityp3"};
	String[] modifyClasses=new String[]{"probsym.Trityp"
			,"probsym.modify.Trityp1","probsym.modify.Trityp2","probsym.modify.Trityp3"};
	String[] methods=new String[]{"classify(sym#sym#sym)"};
	
	@Before
	public void setup() throws FileNotFoundException, IOException{
		conf.put("constraint.store.basePath", "store/constraint");
		conf.load(new FileInputStream(new File("src/examples/Trityp.jpf")));
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

	
	
	


}
