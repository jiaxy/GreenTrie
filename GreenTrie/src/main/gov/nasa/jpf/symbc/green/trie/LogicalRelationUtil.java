package gov.nasa.jpf.symbc.green.trie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;

public class LogicalRelationUtil {
	static Set<Operation.Operator> opset = new HashSet<Operation.Operator>();
	static {
		opset.add(Operation.Operator.LE);
		opset.add(Operation.Operator.GE);
		opset.add(Operation.Operator.EQ);
		opset.add(Operation.Operator.NE);
	}

	public static boolean imply(Operation left, Operation right) {
		Operator lop = left.getOperator();
		Operator rop = right.getOperator();
		if (lop == Operator.NE || rop == Operator.EQ || !opset.contains(rop)
				|| !opset.contains(lop)) {
			return false;
		}
		if (!"0".equals(left.getOperand(1).toString())
				|| !"0".equals(right.getOperand(1).toString())) {
			return false;
		}
		Expression e1 = left.getOperand(0);
		Expression e2 = right.getOperand(0);
		if (!(e1 instanceof Operation)
				|| !(e2 instanceof Operation)
				|| !((Operation) e1).getOperator().equals(
						Operation.Operator.ADD)
				|| !((Operation) e2).getOperator().equals(
						Operation.Operator.ADD)) {
			return false;
		}
		Expression e11 = ((Operation) e1).getOperand(0);
		Expression e21 = ((Operation) e2).getOperand(0);
		if (!e11.equals(e21)) {
			return false;
		}
		Expression t1 = ((Operation) e1).getOperand(1);
		Expression t2 = ((Operation) e2).getOperand(1);
		if (!(t1 instanceof IntConstant) || !(t2 instanceof IntConstant)) {
			return false;
		}
		int n1 = ((IntConstant) t1).getValue();
		int n2 = ((IntConstant) t2).getValue();
		switch (lop) {
		case EQ:
			if (rop == Operator.NE) {
				return n1 != n2; // x=-n1,-n1!=-n2 -> x!=-n2
			} else if (rop == Operator.LE) {
				return n1 >= n2; // x=-n1,-n1<=-n2 -> x<=-n2
			} else if (rop == Operator.GE) {
				return n1 <= n2; // x=-n1, -n1>=-n2 -> x>=-n2
			}
			break;
		case LE:
			if (rop == Operator.NE) {
				return n1 > n2; // x<=-n1,-n1<-n2 -> x!=-n2
			} else if (rop == Operator.LE) {
				return n1 >= n2; // x<=-n1,-n1<=-n2 -> x<=-n2
			}
			break;
		case GE:
			if (rop == Operator.NE) {
				return n1 < n2; // x>=-n1,-n1>-n2 -> x!=-n2
			} else if (rop == Operator.GE) {
				return n1 <= n2; // x>=-n1,-n1>=-n2 -> x>=-n2
			}
		}
		return false;
	}

	public static List<Operation> logicallyReduce(List<Operation> expList)
			throws InvalidIntervalException {
		
		List<Operation> result = new ArrayList<Operation>();
		IntCompareOperation pre = null;
		IntCompareOperation cur = null;
		for (int i = 0; i < expList.size(); i++) {
			Operation o = expList.get(i);
			cur = IntCompareOperation.getInstance(o);
			if (cur == null) {
				result.add(o);
			} else if (pre == null ){
				pre = cur;
			}else if (pre.prefix.equals(cur.prefix)){
				pre.mergeWith(cur);
			}else {
				result.addAll(pre.toOperationList());	
				pre = cur;
			}
		}
		if(pre!=null){
			result.addAll(pre.toOperationList());	
		}else{
			assert(false);
		}
		
		return result;
	}

	public static Expression getCononizedPrefix(Operation exp) {
		if (isIntCompareOperation(exp)) {
			Expression e1 = exp.getOperand(0);
			return ((Operation) e1).getOperand(0);
		} else {
			return exp;
		}
	}

	public static void setCononizedPrefix(Operation exp, Expression prefix) {
		if (isIntCompareOperation(exp)) {
			Expression e1 = exp.getOperand(0);
			((Operation) e1).setOperand(prefix, 0);
		}
	}

	public static Operation insertIntoImplyGraph(List<Operation> heads,
			Operation newExp) {
		Operation tobeReplace = null;
		for (Operation head : heads) {
			if (head.equals(newExp)) {
				return head;
			} else if (imply(head, newExp)) {
				return insertIntoImplyGraph(head.getImply(), newExp);
			} else if (imply(newExp, head)) {
				List<Operation> im = newExp.getImply();
				if (!im.contains(head)) {
					im.add(head);
				}
				tobeReplace = head;
				break;
			}
		}
		if (tobeReplace != null) {
			heads.remove(tobeReplace);
		}
		heads.add(newExp);
		return newExp;
	}

	public static List<Operation> getImply(List<Operation> heads, Operation exp) {
		List<Operation> result = new ArrayList<Operation>();
		for (Operation head : heads) {
			if (exp.equals(head)) {
				addTolist(head, result);
				return result;
			} else if (imply(exp, head)) {
				result.add(exp);
				addTolist(head, result);
				return result;
			} else if (imply(head, exp)) {
				List<Operation> r2 = getImply(head.getImply(), exp);
				if (!r2.isEmpty()) {
					return r2;
				}
			}
		}
		return result;
	}

	public static void addTolist(Operation exp, List<Operation> result) {
		result.add(exp);
		for (Operation o : exp.getImply()) {
			addTolist(o, result);
		}
	}

	public static List<Operation> getBeImplied(List<Operation> heads,
			Operation exp) {
		List<Operation> result=new ArrayList<Operation>();
		List<List<Operation>> queue=new LinkedList<List<Operation>>();
		queue.add(heads);
		while(!queue.isEmpty()){
			List<Operation> list = queue.remove(0);
			for (Operation head : list) {
				if (exp.equals(head)||imply(head, exp)) {
					if(!result.contains(head)){
						result.add(head);
						queue.add(head.getImply());
					}
				}
			}
		}
		
		
//		for (Operation head : heads) {
//			if (exp.equals(head)) {
//				result.add(head);
//				return result;
//			} else if (imply(head, exp)) {
//				if (!result.contains(head)) {
//					Set<Operation> r2 = getBeImplied(head.getImply(), exp);
//					result.addAll(r2);
//					result.add(head);
//				}
//			}
//		}
		return result;
	}

	// public static Boolean constantCheck(Operation exp) {
	// if(!(exp.getOperand(0) instanceof IntConstant)||!(exp.getOperand(1)
	// instanceof IntConstant)){
	// return null;
	// }
	// IntConstant c1=(IntConstant) exp.getOperand(0);
	// IntConstant c2=(IntConstant) exp.getOperand(1);
	// if(exp.getOperator().equals(Operator.LE)){
	// return c1.getValue()<=c2.getValue();
	// }
	// if(exp.getOperator().equals(Operator.EQ)){
	// return c1.getValue()==c2.getValue();
	// }
	// if(exp.getOperator().equals(Operator.NE)){
	// return c1.getValue()!=c2.getValue();
	// }
	// return null;
	// }

	// private static boolean isPrefixEqual(Expression e1, Expression e2, double
	// n1,
	// double n2) {
	// Expression e11 = (n1 == 0) ? e1 : ((Operation) e1).getOperand(0);
	// Expression e21 = (n2 == 0) ? e2 : ((Operation) e2).getOperand(0);
	// return e11.equals(e21);
	// }
	//
	// private static double getContantValue(Expression e1) {
	// double n1 = 0;
	// if (e1 instanceof Operation
	// && ((Operation) e1).getOperator()
	// .equals(Operation.Operator.ADD)) {
	// Expression t = ((Operation) e1).getOperand(1);
	// if (t instanceof IntConstant) {
	// n1 = ((IntConstant) t).getValue();
	// } else if (t instanceof RealConstant) {
	// n1 = ((RealConstant) t).getValue();
	// }
	// }
	// return n1;
	// }

	static boolean isIntCompareOperation(Operation exp) {
		if (!LogicalRelationUtil.opset.contains(exp.getOperator())
				|| !"0".equals(exp.getOperand(1).toString())) {
			return false;
		}
		Expression e1 = exp.getOperand(0);
		if (!(e1 instanceof Operation)
				|| !((Operation) e1).getOperator().equals(
						Operation.Operator.ADD)) {
			return false;
		}
		Expression t1 = ((Operation) e1).getOperand(1);
		if (!(t1 instanceof IntConstant)) {
			return false;
		}
		return true;
	}

}


