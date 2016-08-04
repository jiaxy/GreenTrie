package cn.edu.whu.sklse.greentrie.store;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;

import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;

public class LTrieTest {
	//LTrie ltrie=new LTrie("./store/sat");
	LTrie ltrie=new LTrie();
	@Before
	public void setup(){
		
	}

	@Test
	public void testSave() {
		//LTrie ltrie=new LTrie();
		List<Operation> opList=new ArrayList<Operation>();
		IntConstant zero = new IntConstant(0);
		IntVariable v0 = new IntVariable("v0", 0, 99);
		IntVariable v1 = new IntVariable("v1", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v1, new IntConstant(-1));
		Operation o2 = new Operation(Operation.Operator.ADD, v1, new IntConstant(-2));
		Operation o3 = new Operation(Operation.Operator.ADD, v0, new IntConstant(-3));
		Operation o4 = new Operation(Operation.Operator.ADD, v0, new IntConstant(5));
		Operation o5 = new Operation(Operation.Operator.ADD, v0, new IntConstant(-4));
		Operation o6 = new Operation(Operation.Operator.ADD, v0, v1);
		Operation o7= new Operation(Operation.Operator.ADD, o6, new IntConstant(-1));
	
		Operation c1= new Operation(Operation.Operator.LE, o1, zero);//v1+(-1)<=0
		Operation c2= new Operation(Operation.Operator.NE, o2, zero);//v1+(-2)!=0
		Operation c3= new Operation(Operation.Operator.EQ, o3, zero);//v0+(-3)=0
		Operation c4= new Operation(Operation.Operator.GE, o4, zero);//v0+5>=0
		Operation c5= new Operation(Operation.Operator.EQ, o5, zero);//v0+(-4)=0
		Operation c6= new Operation(Operation.Operator.LE, o6, zero);//v0+v1<=0
		Operation c7= new Operation(Operation.Operator.LE, o7, zero);//v0+v1+(-1)<=0
		
		opList.add(c4);
		Map<String,Object> solution=new HashMap<String,Object>();
		solution.put("v0",4);
		ltrie.saveConstraint(opList, solution, true);
		
		opList.clear();
		solution.clear();
		opList.add(c3);
		opList.add(c6);
		solution.put("v0",0);
		solution.put("v1",-1);
		ltrie.saveConstraint(opList, solution, true);
		
		
		ltrie.shutdown();		
	}
	
	private void saveSomething(){
		List<Operation> opList=new ArrayList<Operation>();
		IntConstant zero = new IntConstant(0);
		IntVariable v0 = new IntVariable("v0", 0, 99);
		IntVariable v1 = new IntVariable("v1", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v1, new IntConstant(-1));
		Operation o2 = new Operation(Operation.Operator.ADD, v1, new IntConstant(-2));
		Operation o3 = new Operation(Operation.Operator.ADD, v0, new IntConstant(-3));
		Operation o4 = new Operation(Operation.Operator.ADD, v0, new IntConstant(5));
		Operation o5 = new Operation(Operation.Operator.ADD, v0, new IntConstant(-4));
		Operation o6 = new Operation(Operation.Operator.ADD, v0, v1);
		Operation o7= new Operation(Operation.Operator.ADD, o6, new IntConstant(-1));
	
		Operation c1= new Operation(Operation.Operator.LE, o1, zero);//v1+(-1)<=0
		Operation c2= new Operation(Operation.Operator.NE, o2, zero);//v1+(-2)!=0
		Operation c3= new Operation(Operation.Operator.EQ, o3, zero);//v0+(-3)=0
		Operation c4= new Operation(Operation.Operator.GE, o4, zero);//v0+5>=0
		Operation c5= new Operation(Operation.Operator.EQ, o5, zero);//v0+(-4)=0
		Operation c6= new Operation(Operation.Operator.LE, o6, zero);//v0+v1<=0
		Operation c7= new Operation(Operation.Operator.LE, o7, zero);//v0+v1+(-1)<=0
		
		opList.add(c5);
		Map<String,Object> solution=new HashMap<String,Object>();
		solution.put("v0",4);
		ltrie.saveConstraint(opList, solution, true);
		
		opList.clear();
		solution.clear();
		opList.add(c4);
		opList.add(c6);
		solution.put("v0",0);
		solution.put("v1",-1);
		ltrie.saveConstraint(opList, solution, true);
		
		opList.clear();
		solution.clear();
		opList.add(c4);
		opList.add(c1);
		solution.put("v0",0);
		solution.put("v1",-5);
		ltrie.saveConstraint(opList, solution, true);
		
		opList.clear();
		solution.clear();
		opList.add(c3);
		opList.add(c1);
		opList.add(c7);
		solution.put("v0",4);
		solution.put("v1",-3);
		ltrie.saveConstraint(opList, solution, true);
		
		opList.clear();
		solution.clear();
		opList.add(c3);
		opList.add(c2);
		solution.put("v0",3);
		solution.put("v1",0);
		ltrie.saveConstraint(opList, solution, true);
		
	}
	
	
	@Test
	public void testGetIS() {
		saveSomething();
		IntVariable v0 = new IntVariable("v0", 0, 99);
		Operation c1= new Operation(Operation.Operator.GE, v0, new IntConstant(0));//v0>=0
		List<Vertex> s = ltrie.getIS(c1);
		for(Vertex v:s){
			System.out.println(v.getProperty(LTrie.PROP_EXP));
		}
	}
	
	@Test
	public void testGetRIS() {
		saveSomething();
		IntVariable v0 = new IntVariable("v0", 0, 99);
		Operation c1= new Operation(Operation.Operator.GE, v0, new IntConstant(0));//v0>=0
		List<Vertex> s = ltrie.getRIS(c1);
		for(Vertex v:s){
			System.out.println(v.getProperty(LTrie.PROP_EXP));
		}
	}
	
	@Test
	public void testQuerySuperset() {
		saveSomething();
		IntVariable v0 = new IntVariable("v0", 0, 99);
		IntVariable v1 = new IntVariable("v1", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v0, v1);
		Operation o2 = new Operation(Operation.Operator.ADD, v0, new IntConstant(6));
		Operation c1= new Operation(Operation.Operator.GE, o2, new IntConstant(0));//v0+6>=0
		Operation c2= new Operation(Operation.Operator.LE, o1, new IntConstant(0));//v0+v1<=0
		List<Operation> oplist=new ArrayList<Operation>();
		oplist.add(c1);
		oplist.add(c2);
		Vertex v = ltrie.querySuperset(oplist);
		System.out.println(v);
	
	}
	
	@Test
	public void testQuerySubset() {
		saveSomething();
		IntVariable v0 = new IntVariable("v0", 0, 99);
		IntVariable v1 = new IntVariable("v1", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v0, v1);
		Operation c1= new Operation(Operation.Operator.GE, v0, new IntConstant(0));//v0>=0
		Operation c2= new Operation(Operation.Operator.LE, o1, new IntConstant(0));//v0+v1<=0
		List<Operation> oplist=new ArrayList<Operation>();
		oplist.add(c1);
		oplist.add(c2);
		Vertex v = ltrie.querySubset(oplist);
		System.out.println(v);
	
	}



}
