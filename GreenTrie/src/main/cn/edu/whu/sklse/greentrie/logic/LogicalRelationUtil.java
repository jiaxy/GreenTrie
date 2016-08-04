package cn.edu.whu.sklse.greentrie.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.edu.whu.sklse.SimpleProfiler;
import cn.edu.whu.sklse.greentrie.canolize.NumberUtil;
import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;
import za.ac.sun.cs.green.expr.RealConstant;

public class LogicalRelationUtil {
	static Set<Operation.Operator> opset = new HashSet<Operation.Operator>();
	static {
		opset.add(Operation.Operator.LE);
		opset.add(Operation.Operator.GE);
		opset.add(Operation.Operator.EQ);
		opset.add(Operation.Operator.NE);
	}
	
	
	static public Set<Operation.Operator> comparators = new HashSet<Operation.Operator>();
	static {
		comparators.add(Operation.Operator.EQ);
		comparators.add(Operation.Operator.NE);
		comparators.add(Operation.Operator.LT);
		comparators.add(Operation.Operator.GT);
		comparators.add(Operation.Operator.LE);
		comparators.add(Operation.Operator.GE);
	}

	public static boolean imply(Operation left, Operation right) {
		SimpleProfiler.start("imply");
		boolean result=false;
		if(comparators.contains(left.getOperator())&&comparators.contains(right.getOperator())){
			
			result= NumberComparasionImply(left,right);
		}else{
			//TODO
		}
		SimpleProfiler.stop("imply");
		return result;
	}
	
	
	public static boolean NumberComparasionImply(Operation left, Operation right) {
		
		Expression pref1=getPrefix(left);
		Expression pref2=getPrefix(right);
		if(!pref1.equals(pref2)){
			return false;
		}
		Operator lop = left.getOperator();
		Operator rop = right.getOperator();
		Number n1 = getConstantValue(left, pref1);
		Number n2 = getConstantValue(right, pref2);
		double dif=NumberUtil.compare(n1, n2);
		
		switch (lop) {
		case EQ:
			if (rop == Operator.NE) {
				return dif!=0; // x=-n1,-n1!=-n2 -> x!=-n2
			} else if (rop == Operator.LE) {
				return dif>=0; // x=-n1,-n1<=-n2 -> x<=-n2
			} else if (rop == Operator.GE) {
				return dif<=0; // x=-n1, -n1>=-n2 -> x>=-n2
			}
			break;
		case LE:
			if (rop == Operator.NE) {
				return dif>0; // x<=-n1,-n1<-n2 -> x!=-n2
			} else if (rop == Operator.LE) {
				return dif>=0; // x<=-n1,-n1<=-n2 -> x<=-n2
			} else if (rop == Operator.LT) {
				return dif>0; // x<=-n1,-n1<-n2 -> x<-n2
			}
			break;
		case GE:
			if (rop == Operator.NE) {
				return dif<0; // x>=-n1,-n1>-n2 -> x!=-n2
			} else if (rop == Operator.GE) {
				return dif<=0; // x>=-n1,-n1>=-n2 -> x>=-n2
			}else if (rop == Operator.GT) {
				return dif<0; // x>=-n1,-n1>-n2 -> x>-n2
			}
		case LT:
			if (rop == Operator.NE) {
				return dif>=0; // x<-n1,-n1<=-n2 -> x!=-n2
			} else if (rop == Operator.LE) {
				return dif>=0; // x<-n1,-n1<=-n2 -> x<=-n2
			} else if (rop == Operator.LT) {
				return dif>=0; // x<-n1,-n1<=-n2 -> x<-n2
			}
			break;
		case GT:
			if (rop == Operator.NE) {
				return dif<=0; // x>-n1,-n1>=-n2 -> x!=-n2
			} else if (rop == Operator.GE) {
				return dif<=0; // x>-n1,-n1>=-n2 -> x>=-n2
			}else if (rop == Operator.GT) {
				return dif<=0; // x>-n1,-n1>=-n2 -> x>-n2
			}
		default:
			break;
		}
		return false;
	}


	private static Number getConstantValue(Operation left, Expression pref) {
		if(pref!=left.getOperand(0)){
			Operation l = (Operation)left.getOperand(0);
			if(l.getOperator()==Operator.ADD){
				Expression con = l.getOperand(1) ;
				if(con instanceof IntConstant){
					return ((IntConstant) con).getValue();
				}else if(con instanceof RealConstant){
					return ((RealConstant) con).getValue();
				}
			}
		}
		return 0;
	}
	

	public static Expression getPrefix(Operation exp) {
		if (comparators.contains(exp.getOperator())) {
			Expression e1 = exp.getOperand(0);
			if(e1 instanceof Operation){
				Operation op=(Operation) e1;
				if(op.getOperator()==Operation.Operator.ADD){
					if(op.getOperand(1) instanceof IntConstant ||op.getOperand(1) instanceof RealConstant){
						return op.getOperand(0);
					}
				}
			}
			return e1;
		} else {
			return exp;
		}
	}

	public static void setPrefix(Operation exp, Expression prefix) {
		if (comparators.contains(exp.getOperator())) {
			Expression e1 = exp.getOperand(0);
			if(e1 instanceof Operation){
				Operation op = (Operation) e1;
				if(op.getOperator()==Operator.ADD){
					if(op.getOperand(1) instanceof Constant){
						((Operation) e1).setOperand(prefix, 0);
						return;
					}
				}
				
			}
			exp.setOperand(prefix, 0);	
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

	private static void addTolist(Operation exp, List<Operation> result) {
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

//	static boolean isCompareOperation(Operation exp) {
//		if (!LogicalRelationUtil.comparators.contains(exp.getOperator())
//				|| !"0".equals(exp.getOperand(1).toString())) {
//			return false;
//		}
////		Expression e1 = exp.getOperand(0);
////		if (!(e1 instanceof Operation)
////				|| !((Operation) e1).getOperator().equals(
////						Operation.Operator.ADD)) {
////			return false;
////		}
////		Expression t1 = ((Operation) e1).getOperand(1);
////		if (!(t1 instanceof IntConstant)) {
////			return false;
////		}
//		return true;
//	}
	public static Operation buildOperationFromList(List<Operation> expList) {
		Operation result=null;
		for(Operation o:expList){
			if (result==null){
				result=o;
			}else{
				result=new Operation(Operator.AND,result,o);
			}
		}
		return result;
	}
	
	
	public static void splitIntoList(List<Operation> expList, Operation exp) {
		if (exp.getOperator().equals(Operation.Operator.AND)) {
			Iterator<Expression> itr = exp.getOperands().iterator();
			while (itr.hasNext()) {
				Operation exp2 = (Operation) itr.next();
				splitIntoList(expList, exp2);
			}
		} else {
			expList.add(exp);
		}
	}
}


