package cn.edu.whu.sklse.greentrie.coral;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import symlib.SymBool;
import symlib.SymDouble;
import symlib.SymInt;
import symlib.SymIntLiteral;
import symlib.Util;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;
import cn.edu.whu.sklse.greentrie.canolize.NumberUtil;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

class CoralTranslator extends Visitor {

	private Stack<Object> stack = null;

	// private List<Object> domains = null;

	private Map<Variable, Object> v2e = null;

	public CoralTranslator() {
		stack = new Stack<Object>();
		v2e = new HashMap<Variable, Object>();
		// domains = new LinkedList<BoolExpr>();
	}

	public SymBool getTranslation() {
		SymBool result = (SymBool) stack.pop();
		return result;
	}

	public Map<Variable, Object> getVariableMap() {
		return v2e;
	}

	@Override
	public void postVisit(IntConstant constant) {
		stack.push(Util.createConstant(constant.getValue()));
	}

	@Override
	public void postVisit(RealConstant constant) {
		stack.push(Util.createConstant(constant.getValue()));
	}

	@Override
	public void postVisit(IntVariable variable) {
		Object v = v2e.get(variable);
		if (v == null) {
			v = Util.createSymLiteral(0);
			v2e.put(variable, v);
		}
		stack.push(v);
	}

	@Override
	public void postVisit(RealVariable variable) {
		Object v = v2e.get(variable);
		if (v == null) {
			v = Util.createSymLiteral(0d);
			v2e.put(variable, v);
		}
		stack.push(v);
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
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.eq((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.eq((SymDouble) l, (SymDouble) r);
			} else if (l instanceof SymBool && r instanceof SymBool) {
				result = Util.eq((SymBool) l, (SymBool) r);
			}
			break;
		case NE:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.ne((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.ne((SymDouble) l, (SymDouble) r);
			} else if (l instanceof SymBool && r instanceof SymBool) {
				result = Util.ne((SymBool) l, (SymBool) r);
			}
			break;
		case LT:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.lt((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.lt((SymDouble) l, (SymDouble) r);
			}
			break;
		case LE:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.le((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.le((SymDouble) l, (SymDouble) r);
			}
			break;
		case GT:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.gt((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.gt((SymDouble) l, (SymDouble) r);
			}
			break;
		case GE:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.ge((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.ge((SymDouble) l, (SymDouble) r);
			}
			break;
		case AND:
			if (l instanceof SymBool && r instanceof SymBool) {
				result = Util.and((SymBool) l, (SymBool) r);
			}
			break;
		case OR:
			if (l instanceof SymBool && r instanceof SymBool) {
				result = Util.or((SymBool) l, (SymBool) r);
			}
			break;
		case ADD:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.add((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.add((SymDouble) l, (SymDouble) r);
			}
			break;
		case SUB:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.sub((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.sub((SymDouble) l, (SymDouble) r);
			}
			break;
		case MUL:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.mul((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.mul((SymDouble) l, (SymDouble) r);
			}
			break;
		case DIV:
			if (l instanceof SymInt && r instanceof SymInt) {
				result = Util.div((SymInt) l, (SymInt) r);
			} else if (l instanceof SymDouble && r instanceof SymDouble) {
				result = Util.div((SymDouble) l, (SymDouble) r);
			}
			break;
		case ABS:
			result = Util.sqrt(Util.mul((SymDouble) l, (SymDouble) l));
			break;
		case SQRT:
			result = Util.sqrt((SymDouble) l);
			break;
		case SIN:
			result = Util.sin( (SymDouble) l);
			break;
		case COS:
			result = Util.cos((SymDouble) l);
			break;
		case TAN:
			result = Util.tan( (SymDouble) l);
			break;
		case ASIN:
			result = Util.asin((SymDouble) l);
			break;
		case ACOS:
			result = Util.acos( (SymDouble) l);
			break;
		case ATAN:
			result = Util.atan((SymDouble) l);
		case ROUND:
			result = Util.round( (SymDouble) l);
			break;
		case LOG:
			result = Util.log((SymDouble) l);
			break;
		case EXP:
			result = Util.exp((SymDouble) l);
			break;
			
		default:

		}
		if (result == null) {
			throw new RuntimeException("Connot translate into Coral expression: " + l + operation.getOperator() + r);
		}else{
			stack.push(result);
		}

	}
}