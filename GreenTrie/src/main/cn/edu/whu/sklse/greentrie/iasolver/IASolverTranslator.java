package cn.edu.whu.sklse.greentrie.iasolver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;
import cn.edu.whu.sklse.greentrie.canolize.NumberUtil;



class IASolverTranslator extends Visitor {
	String format = "%20.10f";
	String pb="";
	

	private Stack<Object> stack = null;

	// private List<Object> domains = null;

	private Map<Variable, Object> v2e = null;

	public IASolverTranslator() {
		stack = new Stack<Object>();
		v2e = new HashMap<Variable, Object>();
		// domains = new LinkedList<BoolExpr>();
	}

	public String getTranslation() {
		String result = (String) stack.pop();
		return pb+result;
	}

	public Map<Variable, Object> getVariableMap() {
		return v2e;
	}

	@Override
	public void postVisit(IntConstant constant) {
		stack.push(constant.getValue());
	}

	@Override
	public void postVisit(RealConstant constant) {
		stack.push( String.format(format,constant.getValue()));
	}

	@Override
	public void postVisit(IntVariable variable) {
		Object v = v2e.get(variable);
		String name=variable.getName();
		if (v == null) {
			Integer max=variable.getUpperBound();
			Integer min=variable.getLowerBound();
			pb = pb + name + " >= " + min + "; "+ name + " <= " + max + "; ";
			v2e.put(variable, name);
		}
		stack.push(name);
	}

	@Override
	public void postVisit(RealVariable variable) {
		Object v = v2e.get(variable);
		String name=variable.getName();
		if (v == null) {
			Double max=variable.getUpperBound();
			Double min=variable.getLowerBound();
			pb = pb + name + " >= " + String.format(format,min) + "; "+ name + " <= " + String.format(format,max) + "; ";
			v2e.put(variable, name);
		}
		stack.push(name);
	}

	@Override
	public void postVisit(Operation operation) throws VisitorException {
		Object l = null;
		Object r = null;
		int arity = operation.getOperator().getArity();
		if (arity == 2) {
			if (!stack.isEmpty()) {
				r = stack.pop();
			}
			if (!stack.isEmpty()) {
				l = stack.pop();
			}
		} else if (arity == 1) {
			if (!stack.isEmpty()) {
				l = stack.pop();
			}
		}
		Object result = null;
		switch (operation.getOperator()) {
		case EQ:
			result=l + " = " + r+"; ";
			break;
		case NE:
			result=l + " != " + r+"; ";
			break;
		case LT:
			result=l + " < " + r+"; ";
			break;
		case LE:
			result=l + " <= " + r+"; ";
			break;
		case GT:
			result=l + " > " + r+"; ";
			break;
		case GE:
			result=l + " >= " + r+"; ";
			break;
		case AND:
			result=""+l + r;
			break;
		case ADD:
			result="("+l + "+" + r +")";
			break;
		case SUB:
			result="("+l + "-" + r +")";
			break;
		case MUL:
			result="("+l + "*" + r +")";
			break;
		case DIV:
			result="("+l + "/" + r +")";
			break;
		case SIN:
			result="sin("+l+")";
			break;
		case COS:
			result="cos("+l+")";
			break;
		case TAN:
			result="tan("+l+")";
			break;
		case ASIN:
			result="asin("+l+")";
			break;
		case ACOS:
			result="acos("+l+")";
			break;
		case ATAN:
			result="atan("+l+")";
			break;
		case LOG:
			result="log("+l+")";
			break;
		case POWER:
			result="("+l + "*" + l +")";
			break;
		case SQRT:
			result="("+l + "^" + 0.5 +")";
			break;
		case ABS:
			result="(("+l+"*"+l+")" + "^" + 0.5 +")";
			break;
//			
//			ABS("ABS", 1),//added by Jia
//			SIN("SIN", 1),
//			COS("COS", 1),
//			TAN("TAN", 1),
//			ASIN("ASIN", 1),
//			ACOS("ACOS", 1),
//			ATAN("ATAN", 1),
//			ATAN2("ATAN2", 2),
//			ROUND("ROUND", 1),
//			LOG("LOG", 1),
//			EXP("EXP", 1),
//			POWER("POWER", 1),
//			SQRT("SQRT", 1),
		default:

		}
		if (result == null) {
			throw new RuntimeException("Connot translate into iasolver expression: " + l + operation.getOperator() + r);
		}else{
			stack.push(result);
		}

	}
}