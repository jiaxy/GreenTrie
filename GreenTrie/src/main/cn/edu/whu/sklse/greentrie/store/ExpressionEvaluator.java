package cn.edu.whu.sklse.greentrie.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.StringConstant;
import za.ac.sun.cs.green.expr.StringVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

public class ExpressionEvaluator {
	
	public static Object evaluate(Expression exp, Map<String, Object> values) {
		Evaluator evaluator = new Evaluator(values);
		try {
			exp.accept(evaluator);
			return evaluator.getResult();
		} catch (VisitorException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("evaluate failed for:" +exp);
		}
		
	}
	
	
}

class Evaluator extends Visitor {

	Stack<Object> stack = new Stack<Object>();
	Map<String, Object> valuesMap = new HashMap<String, Object>();

	public Evaluator(Map<String, Object> values) {
		super();
		this.valuesMap = values;
	}

	public Object getResult() {
		return stack.peek();
	}



	public void postVisit(Operation op) throws VisitorException {
		List<Object> opvalues=new ArrayList<Object>();
		for(Expression e:op.getOperands()){
			opvalues.add(stack.pop());
		}
		
		switch (op.getOperator()) {
		case GT:
			Number r=(Number) opvalues.get(1);
			Number l=(Number) opvalues.get(0);
			stack.push(l.doubleValue()>r.doubleValue());
			break;
		case GE:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(l.doubleValue() >= r.doubleValue());
			break;
		case LT:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(l.doubleValue() < r.doubleValue());
			break;
		case LE:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(l.doubleValue() <= r.doubleValue());
			break;
		case EQ:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(l.doubleValue() == r.doubleValue());
			break;
		case NE:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(l.doubleValue() != r.doubleValue());
			break;
		case ADD:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(NumberUtil.add(l, r));
			break;
		case SUB:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(NumberUtil.sub(l, r));
			break;
		case MUL:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(NumberUtil.multiply(l, r));
			break;
		case DIV:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(NumberUtil.div(l, r));
			break;
		case AND:
			stack.push((Boolean) opvalues.get(1) &&(Boolean) opvalues.get(0));
			break;
		case OR:
			stack.push((Boolean) opvalues.get(1)||(Boolean) opvalues.get(0));
			break;
		case ABS:
			l=(Number) opvalues.get(0);
			stack.push(Math.abs(l.doubleValue()));
			break;
		case SIN:
			l=(Number) opvalues.get(0);
			stack.push(Math.abs(l.doubleValue()));
			break;
		case COS:
			l=(Number) opvalues.get(0);
			stack.push(Math.abs(l.doubleValue()));
			break;
		case SQRT:
			l=(Number) opvalues.get(0);
			stack.push(Math.sqrt(l.doubleValue()));
			break;
		case EXP:
			l=(Number) opvalues.get(0);
			stack.push(Math.exp(l.doubleValue()));
			break;
		case ASIN:
			l=(Number) opvalues.get(0);
			stack.push(Math.asin(l.doubleValue()));
			break;
		case ACOS:
			l=(Number) opvalues.get(0);
			stack.push(Math.acos(l.doubleValue()));
			break;
		case ATAN:
			l=(Number) opvalues.get(0);
			stack.push(Math.atan(l.doubleValue()));
			break;
		case LOG:
			l=(Number) opvalues.get(0);
			stack.push(Math.log(l.doubleValue()));
			break;
		case TAN:
			l=(Number) opvalues.get(0);
			stack.push(Math.tan(l.doubleValue()));
			break;
		case POWER:
			 r=(Number) opvalues.get(1);
			 l=(Number) opvalues.get(0);
			stack.push(Math.pow(r.doubleValue(),l.doubleValue()));
			break;
		case SHIFTL:
			break;
		case SHIFTR:
			break;
		case SHIFTUR:
			break;
		default:
			break;
		}
		super.postVisit((Expression) op);
	}

	public void postVisit(RealConstant realConstant) throws VisitorException {
		stack.push(realConstant.getValue());
		super.postVisit(realConstant);
	}

	public void postVisit(IntConstant intConstant) throws VisitorException {
		stack.push(intConstant.getValue());
		super.postVisit(intConstant);
	}

	public void postVisit(IntVariable intVariable) throws VisitorException {
		postVisit((Variable) intVariable);
	}
	
	public void postVisit(RealVariable realVariable) throws VisitorException {
		Object value = valuesMap.get(realVariable.getName());
		stack.push(value);
		super.postVisit(realVariable);
	}

	public void postVisit(StringConstant stringConstant) throws VisitorException {
		throw new UnsupportedOperationException("cannot evaluate string!");
	}

	public void postVisit(StringVariable stringVariable) throws VisitorException {
		throw new UnsupportedOperationException("cannot evaluate string!");
	}

	public void postVisit(Variable variable) throws VisitorException {
		Object value = valuesMap.get(variable);
		if (value == null) {
			throw new RuntimeException("no value for SymbolicReal " + variable.getName());
		}
		stack.push(value);
		super.postVisit(variable);
	}

}
