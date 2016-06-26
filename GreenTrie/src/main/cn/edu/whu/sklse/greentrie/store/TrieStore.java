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
	public static Properties config = new Properties();
	public static String basePath = "store/constraint";
	Trie satisfiableTrie = null;
	Trie unsatisfiableTrie = null;
	long maxMatchTime = 0;
	long maxSATTime = 0;
	long maxUNSATTime = 0;
	long maxMissMatchTime = 0;
	long satCount = 0;
	long unsatCount = 0;
	long misCount = 0;
	long satQueryTime = 0;
	long unsatQueryTime = 0;
	long missMatchTime = 0;
	long totalQueryCount = 0;
	long totalQueryTime = 0;
	long RISBuildTime = 0;
	long SATTravel_time = 0;
	long totalSaveCount = 0;
	long totalSaveTime = 0;

	Map<String, Object> maxSATReport = null;
	Map<String, Object> maxUNSATReport = null;

	// ConstraintCache trieCache = null;
	long loadTime = 0;
	String satSolver="";
	

	List<String> UNSATExp = new ArrayList<String>();

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
		String basePath = (String) TrieStore.config.get("constraint.store.basePath");
		if (basePath != null) {
			TrieStore.basePath = basePath;
		}
		satisfiableTrie = (Trie) FileUtil.readObject(TrieStore.basePath, "satisfiableTrie");
		unsatisfiableTrie = (Trie) FileUtil.readObject(TrieStore.basePath, "unsatisfiableTrie");
		if (satisfiableTrie == null) {
			satisfiableTrie = new Trie();
		}
		if (unsatisfiableTrie == null) {
			unsatisfiableTrie = new Trie();
		}
		// config.setProperty("satisfiableTrie", "false");
	}

	@Override
	public void shutdown() {
		FileUtil.saveObject(TrieStore.basePath, "satisfiableTrie", satisfiableTrie);
		FileUtil.saveObject(TrieStore.basePath, "unsatisfiableTrie", unsatisfiableTrie);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "totalSave_Count_Time = " + this.totalSaveCount + ","
				+ totalSaveTime);
		reporter.report(getClass().getSimpleName(), "totalQuery_Count_Time = " + this.totalQueryCount + ","
				+ totalQueryTime);
		reporter.report(getClass().getSimpleName(), "maxMatchTime = " + maxMatchTime);
		reporter.report(getClass().getSimpleName(), "maxMissTime = " + maxMissMatchTime);
		reporter.report(getClass().getSimpleName(), "maxSATTime = " + maxSATTime);
		reporter.report(getClass().getSimpleName(), "maxUNSATTime = " + maxUNSATTime);
		reporter.report(getClass().getSimpleName(), "maxSATReport = " + maxSATReport);
		reporter.report(getClass().getSimpleName(), "maxUNSATReport = " + maxUNSATReport);

		reporter.report(getClass().getSimpleName(), "SAT_Count_Time = " + satCount + "," + satQueryTime);
		reporter.report(getClass().getSimpleName(), "SAT_RISBuildTime = " + RISBuildTime);
		reporter.report(getClass().getSimpleName(), "SAT_Travel_time = " + SATTravel_time);
		reporter.report(getClass().getSimpleName(), "UNSA_Count_Time = " + unsatCount + "," + unsatQueryTime);
		reporter.report(getClass().getSimpleName(), "MISS_Count_Time = " + misCount + "," + missMatchTime);

		reporter.report(getClass().getSimpleName(), "SAT_ConstraintCount = " + satisfiableTrie.getPatternCount());
		reporter.report(getClass().getSimpleName(), "UNSAT_ConstraintCount = " + unsatisfiableTrie.getPatternCount());
		// reporter.report(getClass().getSimpleName(), "SAT_ExpressionCount = "
		// + satisfiableTrie.getExpMap().size());
		// reporter.report(getClass().getSimpleName(),
		// "UNSAT_ExpressionCount = " + unsatisfiableTrie.getExpMap().size());
		reporter.report(getClass().getSimpleName(), "SAT_ImplySubGraphCount = "
				+ satisfiableTrie.getImplicationTreeHeads().size());
		reporter.report(getClass().getSimpleName(), "UNSAT_ImplySubGraphCount = "
				+ unsatisfiableTrie.getImplicationTreeHeads().size());
		// String m="";
		
		 for(String s:UNSATExp){
			 System.out.println(s);
		 }
		// reporter.report(getClass().getSimpleName(), "UNSATExp = " +m);
	}

	@Override
	public Object get(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public String getString(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public Boolean getBoolean(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public void put(String key, Serializable value) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public Integer getInteger(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public Long getLong(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public Float getFloat(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public Double getDouble(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	public Apint getApfloatInteger(String key) {
		throw new UnsupportedOperationException("Sorry, TrieStore does not support this operation!");
	}

	@Override
	public Boolean getBoolean(Expression exp) {
		Boolean result = null;
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
		
		//(expList, (Operation) exp);
//		try {
//			// Collections.sort(expList);
//			expList = new Reducer().reduce(expList);
//			SubSetTask subSetTask = new SubSetTask(unsatisfiableTrie, expList);
//			FutureTask<Boolean> task1 = new FutureTask<Boolean>(subSetTask);
//			SuperSetTask superSetTask = new SuperSetTask(satisfiableTrie, expList);
//			FutureTask<Boolean> task2 = new FutureTask<Boolean>(superSetTask);
//			task1.run();
//			task2.run();
//			if (task1.get()) {
//				unsatCount++;
//				long t1 = System.currentTimeMillis() - t0;
//				unsatQueryTime += t1;
//				if (t1 > this.maxUNSATTime) {
//					this.maxUNSATTime = t1;
//					this.maxUNSATReport = subSetTask.report;
//				}
//				// this.maxUNSATTime=Math.max(this.maxUNSATTime,
//				// System.currentTimeMillis()-t0);
//				result = false;
//			} else if (task2.get()) {
//				satCount++;
////				long t1 = System.currentTimeMillis() - t0;
////				long reverseImplySet_buildTime = (Long) superSetTask.report.get("reverseImplySet_buildTime");
////				long travel_time = (Long) superSetTask.report.get("travel_time");
//				// long total_time=(Long) superSetTask.report.get("total_time");
////				satQueryTime += t1;
////				this.SATTravel_time += travel_time;
////				this.RISBuildTime += reverseImplySet_buildTime;
////				if (t1 > this.maxSATTime) {
////					this.maxSATTime = t1;
////					this.maxSATReport = superSetTask.report;
////				}
//				result = true;
//			}
//
//			if (result == null) {
//				
//				this.misCount++;
//				this.missMatchTime += System.currentTimeMillis() - t0;
//				this.maxMissMatchTime = Math.max(this.maxMissMatchTime, System.currentTimeMillis() - t0);
//				// this.missExp.add(exp.toString());
//				System.out.println("cannot find the result for :" + exp);
//			} else {
//				System.out.println("found " + result + "  result for:" + exp);
//			}
//
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			this.totalQueryCount++;
//			this.totalQueryTime += System.currentTimeMillis() - t0;
//		}
//		System.out.println("found sovling result " + result + " for : " + exp);
//		return result;
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
