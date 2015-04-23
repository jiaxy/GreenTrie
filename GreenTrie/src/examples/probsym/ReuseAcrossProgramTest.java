package probsym;

import static org.junit.Assert.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ReuseAcrossProgramTest {
	
	Config[] configs =null;
	String[] jpfFiles=new String[]{"Trityp.jpf","Euclid.jpf","TCAS.jpf","TreeMap.jpf","BinTree.jpf","BinomialHeap.jpf"	};

	@Before
	public void setup() throws FileNotFoundException, IOException{
		configs=new Config[jpfFiles.length];
		for(int i=0;i<jpfFiles.length;i++){
			Config conf = JPF.createConfig(new String[]{});
			conf.put("constraint.store.basePath", "store/constraint");
			conf.load(new FileInputStream(new File("test/"+jpfFiles[i])));
			configs[i]=conf;
		}
	}

	@Test
	public void testTrityp() {
		List<String> report =new ArrayList<String>();
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[1],true));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[1],false));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[2],true));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[2],false));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[3],true));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[3],false));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[4],true));	
//		report.add(TestUtil.testAcrossProgram(configs[0],configs[4],false));	
		report.add(TestUtil.testAcrossProgram(configs[0],configs[5],true));	
		report.add(TestUtil.testAcrossProgram(configs[0],configs[5],false));	
//		for(int i=0;i<configs.length;i++){
//			if(i!=0){
//				report.add(TestUtil.testAcrossProgram(configs[0],configs[i],true));	
//			}
//		}
		for(String s:report){
			System.out.println(s);
		}
	}

	@Test
	public void testTCAS() {
		List<String> report =new ArrayList<String>();
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[0],true));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[0],false));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[1],true));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[1],false));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[3],true));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[3],false));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[4],true));	
//		report.add(TestUtil.testAcrossProgram(configs[2],configs[4],false));	
		report.add(TestUtil.testAcrossProgram(configs[2],configs[5],true));	
		report.add(TestUtil.testAcrossProgram(configs[2],configs[5],false));	
		for(String s:report){
			System.out.println(s);
		}
	}
	
	@Test
	public void testEuclid() {
		List<String> report =new ArrayList<String>();
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[0],true));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[0],false));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[2],true));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[2],false));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[3],true));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[3],false));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[4],true));	
//		report.add(TestUtil.testAcrossProgram(configs[1],configs[4],false));	
		report.add(TestUtil.testAcrossProgram(configs[1],configs[5],true));	
		report.add(TestUtil.testAcrossProgram(configs[1],configs[5],false));	
		for(String s:report){
			System.out.println(s);
		}
	}
	
	@Test
	public void testTreeMap() {
		List<String> report =new ArrayList<String>();
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[0],true));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[0],false));	
		report.add(TestUtil.testAcrossProgram(configs[3],configs[1],true));	
		report.add(TestUtil.testAcrossProgram(configs[3],configs[1],false));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[2],true));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[2],false));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[4],true));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[4],false));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[5],true));	
//		report.add(TestUtil.testAcrossProgram(configs[3],configs[5],false));	
		for(String s:report){
			System.out.println(s);
		}
	}
	
	@Test
	public void testBinTree() {
		List<String> report =new ArrayList<String>();
		report.add(TestUtil.testAcrossProgram(configs[4],configs[0],true));	
		report.add(TestUtil.testAcrossProgram(configs[4],configs[0],false));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[1],true));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[1],false));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[2],true));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[2],false));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[3],true));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[3],false));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[5],true));	
//		report.add(TestUtil.testAcrossProgram(configs[4],configs[5],false));	
		for(String s:report){
			System.out.println(s);
		}
	}
	
	
	
}
