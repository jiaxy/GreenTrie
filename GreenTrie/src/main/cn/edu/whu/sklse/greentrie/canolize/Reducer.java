package cn.edu.whu.sklse.greentrie.canolize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;
import za.ac.sun.cs.green.expr.RealConstant;
import cn.edu.whu.sklse.greentrie.logic.LogicalRelationUtil;

/**
 * The service which reduce the constraint. But it is weird that
 *         sometimes the reduced constraints will cost much more time to solve
 *         in Z3, the possible reason is that it may disturb the incremental
 *         solving mechanism of Z3
 *         
 * @author jiaxy 
 */
public class Reducer {

	public Expression reduce(Expression exp) {
		if(exp.equals(Operation.TRUE)||exp.equals(Operation.FALSE)){
			return exp;
		}
		List<Operation> expList = new LinkedList<Operation>();
		LogicalRelationUtil.splitIntoList(expList, (Operation) exp);
		expList = reduce(expList);
		Operation result = null;
		for (Operation op : expList) {
			if (result == null) {
				result = op;
			} else {
				result = new Operation(Operator.AND, result, op);
			}
		}
		return result;
	}

	public List<Operation> reduce(List<Operation> opList) {
		Collections.sort(opList);
		Map<Expression, Interval> intervalMap = new TreeMap<Expression, Interval>();
		List<Operation> others = new ArrayList<Operation>();
		for (Operation op : opList) {
			Operator operator = op.getOperator();
			if (LogicalRelationUtil.comparators.contains(operator)) {
				Expression left = op.getOperand(0);
				Expression pref = null;
				Number offset = 0;
				if (left instanceof Operation) {
					Operation lop = (Operation) left;
					if (lop.getOperator() == Operation.Operator.ADD && lop.getOperand(1) instanceof Constant) {
						pref = lop.getOperand(0);
						Expression num = lop.getOperand(1);
						if (num instanceof IntConstant) {
							offset = ((IntConstant) num).getValue();
						} else if (num instanceof RealConstant) {
							offset = ((RealConstant) num).getValue();
						}
					}
				}
				if (pref == null) {
					pref = left;
				}
				if (NumberUtil.containtReal(pref)) {
					offset = offset.doubleValue();
				}
				Interval interval = new Interval(operator, offset);
				Interval preInterval = intervalMap.get(pref);
				if (preInterval == null) {
					intervalMap.put(pref, interval);
				} else {
					preInterval.intersectWith(interval);
				}
			} else {
				others.add(op);
			}
		}

		List<Operation> result = new ArrayList<Operation>();
		for (Expression op : intervalMap.keySet()) {
			Interval interval = intervalMap.get(op);
			interval.clean();
			if (interval.isValid()) {
				if(interval.min.equals(interval.max)){
					result.add(buildOperation(op, NumberUtil.multiply(-1, interval.min), Operator.EQ));
				}else {
					if (!interval.min.equals(Double.NEGATIVE_INFINITY)) {
						Operator o1 = interval.minIsOpen ? Operator.GT : Operator.GE;
						result.add(buildOperation(op, NumberUtil.multiply(-1, interval.min), o1));
					}
					if (!interval.max.equals(Double.POSITIVE_INFINITY)) {
						Operator o1 = interval.maxIsOpen ? Operator.LT : Operator.LE;
						result.add(buildOperation(op, NumberUtil.multiply(-1, interval.max), o1));
					}
				}

				for (Number n : interval.neValues) {
					result.add(buildOperation(op, NumberUtil.multiply(-1, n), Operator.NE));
				}
			} else {
				result.clear();
				result.add((Operation) Operation.FALSE);
				return result;
			}
		}
		result.addAll(others);
		return result;

	}

	private Operation buildOperation(Expression op, Number num, Operator operator) {
		
		Expression left=null;
		if(NumberUtil.containtReal(op)){
			left=(num.doubleValue() == 0.0)?op:new Operation(Operator.ADD, op, new RealConstant(num.doubleValue()));
			return new Operation(operator, left, Operation.REAL_ZERO);
		}else{
			left=(num.intValue() == 0)?op:new Operation(Operator.ADD, op, new IntConstant(num.intValue()));
			return new Operation(operator, left, Operation.ZERO);
		}
	}

	class Interval {
		Number min = Double.NEGATIVE_INFINITY;
		Number max = Double.POSITIVE_INFINITY;
		boolean isInt = true;
		boolean minIsOpen = true;
		boolean maxIsOpen = true;
		Set<Number> neValues = new TreeSet<Number>();

		public Interval(Number min, Number max) {
			super();
			this.min = min;
			this.max = max;
		}

		public Interval(Number neValue) {
			super();
			if (neValue != null) {
				this.neValues.add(neValue);
			}
		}

		public Interval(Operator operator, Number offset) {
			Number num = NumberUtil.multiply(-1, offset);
			switch (operator) {
			case EQ:
				this.max = this.min = num;
				this.minIsOpen = false;
				this.maxIsOpen = false;
				break;
			case NE:
				this.neValues.add(num);
				break;
			case GT:
				this.min = num;
				break;
			case LT:
				this.max = num;
				break;
			case GE:
				this.min = num;
				this.minIsOpen = false;
				break;
			case LE:
				this.max = num;
				this.maxIsOpen = false;
				break;
			default:
				throw new RuntimeException("invalid operator " + operator + "!");
			}
		}

		public void intersectWith(Interval b) {
			double dif1 = NumberUtil.compare(this.min, b.min);
			double dif2 = NumberUtil.compare(this.max, b.max);
			if (dif1 == 0.0) {
				this.minIsOpen = this.minIsOpen || b.minIsOpen;
			} else if (dif1 <0){
				this.min =b.min;
				this.minIsOpen=b.minIsOpen;
			}
			if (dif2 == 0.0) {
				this.maxIsOpen = this.maxIsOpen || b.maxIsOpen;
			} else if(dif2 > 0){
				this.max = b.max;
				this.maxIsOpen=b.maxIsOpen;
			}
			neValues.addAll(b.neValues);
		}

		public boolean isValid() {
			double dif1 = NumberUtil.compare(this.max, this.min);
			if (dif1 < 0) {
				return false;
			} 
			if (dif1 == 0 && (this.maxIsOpen || this.minIsOpen)) {
				return false;
			}
			return true;
		}

		public void clean() {
			if (this.min instanceof Integer && this.minIsOpen) {
				this.min = this.min.intValue() + 1;
				this.minIsOpen = false;
			}
			if (this.max instanceof Integer && this.maxIsOpen) {
				this.max = this.max.intValue() - 1;
				this.maxIsOpen = false;
			}
			List<Number> validNe1 = new ArrayList<Number>();
			for (Number n : this.neValues) {
				Double dif1 = NumberUtil.compare(n, this.min);
				if (dif1 < 0) {
					continue;
				} else if (dif1 == 0 && !this.minIsOpen) {
					if (this.min instanceof Integer) {
						this.min = this.min.intValue() + 1;
					} else {
						this.minIsOpen = true;
					}
				} else {
					validNe1.add(n);
				}
			}
			Set<Number> valideNe2 = new TreeSet<Number>();
			for (int i = validNe1.size() - 1; i >= 0; i--) {
				Double dif2 = NumberUtil.compare(validNe1.get(i), this.max);
				if (dif2 > 0) {
					continue;
				} else if (dif2 == 0 && !this.maxIsOpen) {
					if (this.max instanceof Integer) {
						this.max = this.max.intValue() - 1;
					} else {
						this.maxIsOpen = true;
					}
				} else {
					valideNe2.add(validNe1.get(i));
				}
			}
			this.neValues = valideNe2;
		}

	}

}
