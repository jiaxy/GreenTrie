package cn.edu.whu.sklse.ubtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apfloat.Apint;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.store.ExpressionStore;
import za.ac.sun.cs.green.util.Reporter;
import cn.edu.whu.sklse.greentrie.store.FileUtil;

public class UBTreeStore implements ExpressionStore {
	public static Properties config = new Properties();
	public static String basePath = "store/constraint";
	UBTree<Operation, Map<String, Object>> satisfiableTree = null;
	UBTree<Operation, Map<String, Object>> unsatisfiableTree = null;
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

	List<String> UNSATExp = new ArrayList<String>();

	public UBTreeStore(Green solver, Properties conf) {
		super();
		if (config != null) {
			UBTreeStore.config = config;
		}
		String basePath = (String) UBTreeStore.config
				.get("constraint.store.basePath");
		if (basePath != null) {
			UBTreeStore.basePath = basePath;
		}
		satisfiableTree = (UBTree<Operation, Map<String, Object>>) FileUtil
				.readObject(UBTreeStore.basePath, "satisfiableTree");
		unsatisfiableTree = (UBTree<Operation, Map<String, Object>>) FileUtil
				.readObject(UBTreeStore.basePath, "unsatisfiableTree");
		if (satisfiableTree == null) {
			satisfiableTree = new UBTree<Operation, Map<String, Object>>();
		}
		if (unsatisfiableTree == null) {
			unsatisfiableTree = new UBTree<Operation, Map<String, Object>>();
		}
		// config.setProperty("satisfiableTrie", "false");
	}

	@Override
	public void shutdown() {
		FileUtil.saveObject(UBTreeStore.basePath, "satisfiableTree",
				satisfiableTree);
		FileUtil.saveObject(UBTreeStore.basePath, "unsatisfiableTree",
				unsatisfiableTree);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "totalSave_Count_Time = "
				+ this.totalSaveCount + "," + totalSaveTime);
		reporter.report(getClass().getSimpleName(), "totalQuery_Count_Time = "
				+ this.totalQueryCount + "," + totalQueryTime);
		reporter.report(getClass().getSimpleName(), "maxMatchTime = "
				+ maxMatchTime);
		reporter.report(getClass().getSimpleName(), "maxMissTime = "
				+ maxMissMatchTime);
		reporter.report(getClass().getSimpleName(), "maxSATTime = "
				+ maxSATTime);
		reporter.report(getClass().getSimpleName(), "maxUNSATTime = "
				+ maxUNSATTime);
		reporter.report(getClass().getSimpleName(), "maxSATReport = "
				+ maxSATReport);
		reporter.report(getClass().getSimpleName(), "maxUNSATReport = "
				+ maxUNSATReport);

		reporter.report(getClass().getSimpleName(), "SAT_Count_Time = "
				+ satCount + "," + satQueryTime);
		reporter.report(getClass().getSimpleName(), "SAT_RISBuildTime = "
				+ RISBuildTime);
		reporter.report(getClass().getSimpleName(), "SAT_Travel_time = "
				+ SATTravel_time);
		reporter.report(getClass().getSimpleName(), "UNSA_Count_Time = "
				+ unsatCount + "," + unsatQueryTime);
		reporter.report(getClass().getSimpleName(), "MISS_Count_Time = "
				+ misCount + "," + missMatchTime);

		// String m="";
		// for(String s:UNSATExp){
		// m+=s+"\n";
		// }
		// reporter.report(getClass().getSimpleName(), "UNSATExp = " +m);
	}

	@Override
	public Object get(String key) {
		throw new UnsupportedOperationException(
				"Sorry, TrieStore does not support this operation!");
	}

	@Override
	public String getString(String key) {
		return null;
	}

	@Override
	public Boolean getBoolean(String key) {
		return null;
	}

	@Override
	public void put(String key, Serializable value) {
	}

	@Override
	public Integer getInteger(String key) {
		return null;
	}

	@Override
	public Long getLong(String key) {
		return null;
	}

	@Override
	public Float getFloat(String key) {
		return null;
	}

	@Override
	public Double getDouble(String key) {
		return null;
	}

	public Apint getApfloatInteger(String key) {
		return null;
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
		addToList(expList, (Operation) exp);
		try {
//			Collections.sort(expList);
//			expList = LogicalRelationUtil.reduce(expList);
			SubSetTask2 subSetTask = new SubSetTask2(unsatisfiableTree, expList);
			FutureTask<Boolean> task1 = new FutureTask<Boolean>(subSetTask);
			SuperSetTask2 superSetTask = new SuperSetTask2(satisfiableTree,
					expList);
			FutureTask<Boolean> task2 = new FutureTask<Boolean>(superSetTask);
			task1.run();
			task2.run();
			if (task1.get()) {
				unsatCount++;
				long t1 = System.currentTimeMillis() - t0;
				unsatQueryTime += t1;
				if (t1 > this.maxUNSATTime) {
					this.maxUNSATTime = t1;
					this.maxUNSATReport = subSetTask.report;
				}
				// this.maxUNSATTime=Math.max(this.maxUNSATTime,
				// System.currentTimeMillis()-t0);
				result = false;
			} else if (task2.get()) {
				satCount++;
				long t1 = System.currentTimeMillis() - t0;
//				long reverseImplySet_buildTime = (Long) superSetTask.report
//						.get("reverseImplySet_buildTime");
//				long travel_time = (Long) superSetTask.report
//						.get("travel_time");
				// long total_time=(Long) superSetTask.report.get("total_time");
				satQueryTime += t1;
//				this.SATTravel_time += travel_time;
//				this.RISBuildTime += reverseImplySet_buildTime;
				if (t1 > this.maxSATTime) {
					this.maxSATTime = t1;
					this.maxSATReport = superSetTask.report;
				}
				result = true;
			}

			if (result == null) {
				this.misCount++;
				this.missMatchTime += System.currentTimeMillis() - t0;
				this.maxMissMatchTime = Math.max(this.maxMissMatchTime,
						System.currentTimeMillis() - t0);
				// this.missExp.add(exp.toString());
				System.out.println("cannot find the result for :" + exp);
			} else {
				System.out.println("found " + result + "  result for:" + exp);
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.totalQueryCount++;
			this.totalQueryTime += System.currentTimeMillis() - t0;
		}
		System.out.println("found sovling result " + result + " for : " + exp);
		return result;
	}

	
	@Override
	public Boolean query(Expression exp, Map<String, Object> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(Expression exp, boolean satisfiable,
			Map<String, Object> solution) {
		long t0 = System.currentTimeMillis();
		System.out.println("put into " + satisfiable + " trie: " + exp);
		// if("(((((((((((1*v0)+(-1*v1))+0)<=0)&&((((-1*v0)+(1*v2))+0)<=0))&&((((1*v2)+(-1*v3))+0)<=0))&&((((1*v2)+(-1*v4))+0)!=0))&&((((1*v2)+(-1*v4))+0)==0))&&(((-1*v0)+1)<=0))&&(((-1*v1)+1)<=0))&&(((-1*v2)+1)<=0))&&(((-1*v3)+1)<=0)".equals(exp.toString())){
		// System.out.println("here");
		// }
		if (!(exp instanceof Operation)) {
			return;
		}
		List<Operation> expList = new LinkedList<Operation>();
		addToList(expList, (Operation) exp);
		//			Collections.sort(expList);
		//			expList = LogicalRelationUtil.reduce(expList);
					if (satisfiable) {
						satisfiableTree.insert(expList, solution);
						// addPattern(expList, solution, satisfiable);
					} else {
						unsatisfiableTree.insert(expList, solution);
						// this.UNSATExp.add(exp.toString());
					}
					this.totalSaveCount++;
					this.totalSaveTime += System.currentTimeMillis() - t0;
	}

	private void addToList(List<Operation> expList, Operation exp) {
		if (exp.getOperator().equals(Operation.Operator.AND)) {
			Iterator<Expression> itr = exp.getOperands().iterator();
			while (itr.hasNext()) {
				Operation exp2 = (Operation) itr.next();
				addToList(expList, exp2);
			}
		} else {
			expList.add(exp);
		}
	}
}

class SuperSetTask2 implements Callable<Boolean> {
	UBTree<Operation, Map<String, Object>> tree = null;
	List<Operation> expList = null;
	Map<String, Object> report = new HashMap<String, Object>();

	public SuperSetTask2(UBTree<Operation, Map<String, Object>> trie,
			List<Operation> expList) {
		super();
		this.tree = trie;
		this.expList = expList;
	}

	@Override
	public Boolean call() throws Exception {
		Node<Operation, Map<String, Object>> r = tree.findSuperSet(tree.root,
				expList, 0);
		return (r != null);
	}
}

class SubSetTask2 implements Callable<Boolean> {
	UBTree<Operation, Map<String, Object>> tree = null;
	List<Operation> expList = null;
	Map<String, Object> report = new HashMap<String, Object>();

	public SubSetTask2(UBTree<Operation, Map<String, Object>> trie,
			List<Operation> expList) {
		super();
		this.tree = trie;
		this.expList = expList;
	}

	@Override
	public Boolean call() throws Exception {
		Node<Operation, Map<String, Object>> r = tree.findSubset(tree.root,
				expList, 0);
		return (r != null);
	}
}
