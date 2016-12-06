package cn.edu.whu.sklse.greentrie.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apfloat.Apint;

import cn.edu.whu.sklse.SimpleProfiler;
import cn.edu.whu.sklse.greentrie.canolize.Reducer;
import cn.edu.whu.sklse.greentrie.logic.LogicalRelationUtil;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.store.ExpressionStore;
import za.ac.sun.cs.green.util.Reporter;

public class TrieStore implements ExpressionStore {
	static Properties config = new Properties();
	static String basePath = "store/constraint";
	Trie satisfiableTrie = null;
	Trie unsatisfiableTrie = null;
	long totalSaveCount = 0;
	long totalSaveTime = 0;

	Map<String, Object> maxSATReport = null;
	Map<String, Object> maxUNSATReport = null;

	long loadTime = 0;
	String satSolver="";
	

	List<String> unsatExp = new ArrayList<String>();

	public TrieStore(Green solver, Properties conf) {
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
		String base = (String) TrieStore.config.get("constraint.store.basePath");
		if (base != null) {
			TrieStore.basePath = base;
		}
		satisfiableTrie = (Trie) FileUtil.readObject(TrieStore.basePath, "satisfiableTrie");
		unsatisfiableTrie = (Trie) FileUtil.readObject(TrieStore.basePath, "unsatisfiableTrie");
		if (satisfiableTrie == null) {
			satisfiableTrie = new Trie();
		}
		if (unsatisfiableTrie == null) {
			unsatisfiableTrie = new Trie();
		}

	}

	@Override
	public void shutdown() {
		FileUtil.saveObject(TrieStore.basePath, "satisfiableTrie", satisfiableTrie);
		FileUtil.saveObject(TrieStore.basePath, "unsatisfiableTrie", unsatisfiableTrie);
	}

	@Override
	public void report(Reporter reporter) {
		//print the report!
	}

	@Override
	public Object get(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean getBoolean(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(String key, Serializable value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer getInteger(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getLong(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float getFloat(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Double getDouble(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Apint getApfloatInteger(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean getBoolean(Expression exp) {
		Boolean result;
		if (!(exp instanceof Operation)) {
			return null;
		}
		long t0 = System.currentTimeMillis();
		// if(exp.toString().equals("((((1*v0)+-1)!=0)&&(((1*v0)+-2)!=0))&&(((1*v0)+0)!=0)")){
		// System.out.println("stop");
		// }
		List<Operation> expList = new ArrayList<Operation>();
		LogicalRelationUtil.splitIntoList(expList, (Operation)exp);
		expList = new Reducer().reduce(expList);
		boolean r1=unsatisfiableTrie.hasSubset(expList);
		if(r1){
			System.out.println("found UNSAT result for:" + exp);
			return false;
		}else{
			boolean r2=satisfiableTrie.hasSuperSet(expList);
			if(r2){
				System.out.println("found SAT result for:" + exp);
				return true;
			}
			System.out.println("found no result for:" + exp);
			return null;
		}
	}

	@Override
	public Boolean query(Expression exp,Map<String, Object> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(Expression exp, boolean satisfiable, Map<String, Object> solution) {
		long t0 = System.currentTimeMillis();
		System.out.println("put into " + satisfiable + " trie: " + exp +"with solution:"+solution);
		// if("(((((((((((1*v0)+(-1*v1))+0)<=0)&&((((-1*v0)+(1*v2))+0)<=0))&&((((1*v2)+(-1*v3))+0)<=0))&&((((1*v2)+(-1*v4))+0)!=0))&&((((1*v2)+(-1*v4))+0)==0))&&(((-1*v0)+1)<=0))&&(((-1*v1)+1)<=0))&&(((-1*v2)+1)<=0))&&(((-1*v3)+1)<=0)".equals(exp.toString())){
		// System.out.println("here");
		// }
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
			satisfiableTrie.saveConstraint(expList, solution, satisfiable);
		} else {
			unsatisfiableTrie.saveConstraint(expList, solution, satisfiable);
			//this.UNSATExp.add(exp.toString());
		}
		this.totalSaveCount++;
		this.totalSaveTime += System.currentTimeMillis() - t0;
	}

}

class SuperSetTask implements Callable<Boolean> {
	Trie trie = null;
	List<Operation> expList = null;
	Map<String, Object> report = new HashMap<String, Object>();

	public SuperSetTask(Trie trie, List<Operation> expList) {
		super();
		this.trie = trie;
		this.expList = expList;
	}

	@Override
	public Boolean call() throws Exception {
		
		if(trie.hasSuperSet(expList)) {
			return true;
		}
		//Set<Operation> eset=new HashSet<Operation>();
		//eset.addAll(expList);
		SimpleProfiler.start("valueChecking");
		List<Map<String, Object>> solutions = new ArrayList<Map<String, Object>>();
		trie.getCandidateSuluation(trie.rootState, expList, solutions);
		
		
		for(Map<String, Object> s:solutions){
			System.out.println("check candidate solution: "+s+" for exprssion :"+expList);
			boolean sat=true;
			for(Expression exp:expList){
				Object r = ExpressionEvaluator.evaluate(exp, s);
				if(Boolean.FALSE.equals(r)){
					sat=false;
					break;
				}
			}
			if(sat) {
				System.out.println("found solutions: "+s);
				trie.saveConstraint(expList, s, sat);
				SimpleProfiler.stop("valueChecking");
				return true;
			}
		}
		
		SimpleProfiler.stop("valueChecking");
		return false;
	}

}

class SubSetTask implements Callable<Boolean> {
	Trie trie = null;
	List<Operation> expList = null;
	Map<String, Object> report = new HashMap<String, Object>();

	public SubSetTask(Trie trie, List<Operation> expList) {
		super();
		this.trie = trie;
		this.expList = expList;
	}

	@Override
	public Boolean call() throws Exception {
		return trie.hasSubset(expList);
	}
}
