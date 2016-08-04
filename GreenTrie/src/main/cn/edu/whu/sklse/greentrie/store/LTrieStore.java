package cn.edu.whu.sklse.greentrie.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apfloat.Apint;

import com.tinkerpop.blueprints.Vertex;

import cn.edu.whu.sklse.greentrie.canolize.Reducer;
import cn.edu.whu.sklse.greentrie.logic.LogicalRelationUtil;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.store.ExpressionStore;
import za.ac.sun.cs.green.util.Reporter;

public class LTrieStore implements ExpressionStore {
	public static Properties config = new Properties();
	public static String basePath = "./store";
	long loadTime = 0;
	String satSolver="";
	LTrie SATStore=null;
	LTrie UNSATStore=null;
	
	public LTrieStore(Green solver, Properties conf) {
		super();
		if (config != null) {
			TrieStore.config = config;
		}
		String s=conf.getProperty("green.service.sat");
		if(s.contains("z3")){
			satSolver="z3";
		}else if(s.contains("coral")){
			satSolver="coral";
		}
		String basePath = (String) TrieStore.config.get("constraint.store.basePath");
		if (basePath != null) {
			TrieStore.basePath = basePath;
		}
		SATStore=new LTrie(basePath+"/sat");
		UNSATStore=new LTrie(basePath+"/unsat");
	}
	
	
	@Override
	public void shutdown() {
		SATStore.shutdown();
		UNSATStore.shutdown();
	}

	@Override
	public void report(Reporter reporter) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getBoolean(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInteger(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLong(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getFloat(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getDouble(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Apint getApfloatInteger(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(String key, Serializable value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean getBoolean(Expression exp) {
		if (!(exp instanceof Operation)) {
			return null;
		}
		List<Operation> expList = new ArrayList<Operation>();
		LogicalRelationUtil.splitIntoList(expList, (Operation)exp);
		expList = new Reducer().reduce(expList);
		 Vertex v1 = UNSATStore.querySubset(expList);
		if(v1!=null){
			System.out.println("found UNSAT result for:" + exp);
			return false;
		}else{
			Vertex v2=SATStore.querySuperset(expList);
			if(v2!=null){
				System.out.println("found SAT result for:" + exp);
				return true;
			}
			System.out.println("found no result for:" + exp);
			return null;
		}
	}

	@Override
	public Boolean query(Expression exp, Map<String, Object> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(Expression exp, boolean satisfiable, Map<String, Object> solution) {
		if (!(exp instanceof Operation)) {
			return;
		}
		
		List<Operation> expList = new LinkedList<Operation>();
//		if(!satisfiable){
//			expList=getUnsatCore();
//		}
		
		LogicalRelationUtil.splitIntoList(expList, (Operation)exp);
		//addToList(expList, (Operation) exp);
		// Collections.sort(expList);
		expList = new Reducer().reduce(expList);
		if (satisfiable) {
			SATStore.saveConstraint(expList, solution, satisfiable);
		} else {
			UNSATStore.saveConstraint(expList, solution, satisfiable);
			//this.UNSATExp.add(exp.toString());
		}

	}

}
