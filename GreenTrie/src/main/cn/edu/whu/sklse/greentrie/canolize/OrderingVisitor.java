package cn.edu.whu.sklse.greentrie.canolize;

import java.util.Stack;

import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

class OrderingVisitor extends Visitor {

	private Stack<Expression> stack;
	

	public OrderingVisitor() {
		stack = new Stack<Expression>();
	}

	public Expression getExpression() {
		return stack.pop();
	}

	@Override
	public void postVisit(Constant constant) {
		stack.push(constant);
	}

	@Override
	public void postVisit(Variable variable) {
		stack.push(variable);
	}

	@Override
	public void postVisit(Operation operation) throws VisitorException {
		Operation.Operator op = operation.getOperator();
		Operation.Operator nop = null;
		switch (op) {
		case EQ:
			nop = Operation.Operator.EQ;
			break;
		case NE:
			nop = Operation.Operator.NE;
			break;
		case LT:
			nop = Operation.Operator.GT;
			break;
		case LE:
			nop = Operation.Operator.GE;
			break;
		case GT:
			nop = Operation.Operator.LT;
			break;
		case GE:
			nop = Operation.Operator.LE;
			break;
		default:
			break;
		}
		if (nop != null) {
			Expression r = stack.pop();
			Expression l = stack.pop();
			if ((r instanceof Variable) && (l instanceof Variable)
					&& (((Variable) r).getName().compareTo(((Variable) l).getName()) < 0)) {
				stack.push(new Operation(nop, r, l));
			} else if ((r instanceof Variable) && (l instanceof Constant)) {
				stack.push(new Operation(nop, r, l));
			} else {
				stack.push(operation);
			}
		} else if (op.getArity() == 2) {
			Expression r = stack.pop();
			Expression l = stack.pop();
			stack.push(new Operation(op, l, r));
		} else {
			for (int i = op.getArity(); i > 0; i--) {
				stack.pop();
			}
			stack.push(operation);
		}
	}

}