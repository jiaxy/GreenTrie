package gov.nasa.jpf.symbc.green.trie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;

public class IntCompareOperation {
	List<Operation> exp = new ArrayList<Operation>();
	Expression prefix;
	Interval interval;

	public static IntCompareOperation getInstance(Operation exp) {
		if (LogicalRelationUtil.isIntCompareOperation(exp)) {
			IntCompareOperation inst = new IntCompareOperation();
			Operation e1 = (Operation) exp.getOperand(0);
			inst.exp.add(exp);
			inst.prefix = (e1).getOperand(0);
			IntConstant t1 = (IntConstant) (e1).getOperand(1);
			int value = t1.getValue();
			switch (exp.getOperator()) {
			case LE:
				inst.interval = new Interval(Integer.MIN_VALUE, -value, null);
				break;
			case GE:
				inst.interval = new Interval(-value, Integer.MAX_VALUE, null);
				break;
			case EQ:
				inst.interval = new Interval(-value, -value, null);
				break;
			case NE:
				inst.interval = new Interval(Integer.MIN_VALUE,
						Integer.MAX_VALUE, -value);
			}
			return inst;
		}
		return null;
	}

	public void mergeWith(IntCompareOperation b)
			throws InvalidIntervalException {
		if (this.prefix.equals(b.prefix)) {
			this.interval.intersectWith(b.interval);
			this.exp.addAll(b.exp);
		}
	}

	public List<Operation> toOperationList() {
		if(this.exp.size()==1){
			return this.exp;
		}
		List<Operation> result = new ArrayList<Operation>();
		if(this.interval.min==this.interval.max){
			Operation e1 = new Operation(Operation.Operator.ADD, this.prefix,
					new IntConstant(-this.interval.min));
			result.add(new Operation(Operation.Operator.EQ, e1,	new IntConstant(0)));
			return result;
		}
		if (this.interval.min != Integer.MIN_VALUE) {
			Operation e1 = new Operation(Operation.Operator.ADD, this.prefix,
					new IntConstant(-this.interval.min));
			result.add(new Operation(Operation.Operator.GE, e1,
					new IntConstant(0)));
		}
		if (this.interval.max != Integer.MAX_VALUE) {
			Operation e1 = new Operation(Operation.Operator.ADD, this.prefix,
					new IntConstant(-this.interval.max));
			result.add(new Operation(Operation.Operator.LE, e1,
					new IntConstant(0)));
		}
		for (int i : this.interval.neValues) {
			Operation e1 = new Operation(Operation.Operator.ADD, this.prefix,
					new IntConstant(-i));
			result.add(new Operation(Operation.Operator.NE, e1,
					new IntConstant(0)));
		}
		return result;
	}
}

class Interval {
	int min = 0;
	int max = 0;
	Set<Integer> neValues = new HashSet<Integer>();

	public Interval(int min, int max, Integer neValue) {
		super();
		this.min = min;
		this.max = max;
		if (neValue != null && neValue >= min && neValue <= max) {
			neValues.add(neValue);
		}
	}

	public void intersectWith(Interval b) throws InvalidIntervalException {
		this.min = Math.max(this.min, b.min);
		this.max = Math.min(this.max, b.max);
		if (min > max) {
			throw new InvalidIntervalException();
		}
		Set<Integer> neValues =new HashSet<Integer>();
		for (int i : b.neValues) {
			addneValues(neValues, i);
		}
		for (int i : this.neValues) {
			addneValues(neValues, i);
		}
		this.neValues=neValues;
	}

	private void addneValues(Set<Integer> neValues, int i)
			throws InvalidIntervalException {
		if (this.min == this.max && this.min == i) {
			throw new InvalidIntervalException();
		}
		if(this.min==i){
			this.min++;
		}
		if(this.max==i){
			this.max--;
		}
		if (i > this.min && i < this.max) {
			neValues.add(i);
		}
	}
}

class InvalidIntervalException extends Exception {
	private static final long serialVersionUID = 1L;
}
