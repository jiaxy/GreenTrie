package za.ac.sun.cs.green.service;

import java.util.Map;
import java.util.Set;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.store.ExpressionStore;
import za.ac.sun.cs.green.util.Reporter;

public abstract class SATService extends BasicService {

	private static final String SERVICE_KEY = "SAT:";

	private int invocationCount = 0;

	private int cacheHitCount = 0;

	private int cacheMissCount = 0;

	private long timeConsumption = 0;

	public SATService(Green solver) {
		super(solver);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocationCount = "
				+ invocationCount);
		reporter.report(getClass().getSimpleName(), "cacheHitCount = "
				+ cacheHitCount);
		reporter.report(getClass().getSimpleName(), "cacheMissCount = "
				+ cacheMissCount);
		reporter.report(getClass().getSimpleName(), "timeConsumption = "
				+ timeConsumption);
	}

	@Override
	public Object allChildrenDone(Instance instance, Object result) {
		return instance.getData(getClass());
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		//long t1=System.currentTimeMillis();
		Boolean result = (Boolean) instance.getData(getClass());
		if (result == null) {
			result = solve0(instance);
			if (result != null) {
				instance.setData(getClass(), result);
			}
		}
		//long t2=System.currentTimeMillis();
		//System.out.println("time for processRequest:"+(t2-t1));
		return null;
	}

	private Boolean solve0(Instance instance) {
		long t1=System.currentTimeMillis();
		invocationCount++;
		Boolean result;
		String key = SERVICE_KEY + instance.getFullExpression().toString();
		result = (store instanceof ExpressionStore) ? 
				((ExpressionStore) store)	.getBoolean(instance.getFullExpression()) 
				: store.getBoolean(key);
		if (result == null) {
			cacheMissCount++;
			result = solve1(instance);
			if (result != null) {
				if (store instanceof ExpressionStore) {
					Map<String,Object> solution=(Map<String, Object>) instance.getData("solution");			
					((ExpressionStore) store).put(instance.getFullExpression(), result,solution);
				} else {
					store.put(key, result);
				}
			}
		} else {
			cacheHitCount++;
		}
		long t2=System.currentTimeMillis();
		System.out.println("time for solving:"+(t2-t1));
		return result;
	}

	private Boolean solve1(Instance instance) {
		long startTime = System.currentTimeMillis();
		Boolean result = solve(instance);
		timeConsumption += System.currentTimeMillis() - startTime;
		return result;
	}

	protected abstract Boolean solve(Instance instance);

}
