package cn.edu.whu.sklse.greentrie.canolize;

import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;

import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

class Renamer extends Visitor {

	private Map<Variable, Variable> map;

	private Stack<Expression> stack;

	public Renamer(Map<Variable, Variable> map,
			SortedSet<Variable> variableSet) {
		this.map = map;
		stack = new Stack<Expression>();
	}

	public Expression rename(Expression expression) throws VisitorException {
		expression.accept(this);
		return stack.pop();
	}

	@Override
	public void postVisit(IntVariable variable) {
		Variable v = map.get(variable);
		if (v == null) {
			v = new IntVariable("v" + map.size(), variable.getLowerBound(),
					variable.getUpperBound());
			map.put(variable, v);
		}
		stack.push(v);
	}
	
	@Override
	public void postVisit(RealVariable variable) {
		Variable v = map.get(variable);
		if (v == null) {
			v = new RealVariable("v" + map.size(), variable.getLowerBound(),
					variable.getUpperBound());
			map.put(variable, v);
		}
		stack.push(v);
	}

	@Override
	public void postVisit(Constant constant) {
		stack.push(constant);
	}

	@Override
	public void postVisit(Operation operation) {
		int arity = operation.getOperator().getArity();
		Expression operands[] = new Expression[arity];
		for (int i = arity; i > 0; i--) {
			operands[i - 1] = stack.pop();
		}
		stack.push(new Operation(operation.getOperator(), operands));
	}

}