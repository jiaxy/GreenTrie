package cn.edu.whu.sklse.greentrie.canolize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

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
import za.ac.sun.cs.green.expr.Operation.Operator;

class CanonizationVisitor extends Visitor {
	
	private Stack<Expression> stack;

	private SortedSet<Expression> conjuncts;

	private SortedSet<Variable> variableSet;

	private boolean unsatisfiable;

	public CanonizationVisitor() {
		stack = new Stack<Expression>();
		conjuncts = new TreeSet<Expression>();
		variableSet = new TreeSet<Variable>();
		unsatisfiable = false;
	}

	public SortedSet<Variable> getVariableSet() {
		return variableSet;
	}

	public Expression getExpression() {
		if (unsatisfiable) {
			return Operation.FALSE;
		}
		if (!stack.isEmpty()) {
			Expression x = stack.pop();
			if (!x.equals(Operation.TRUE)) {
				conjuncts.add(x);
			}
		}
		SortedSet<Expression> newConjuncts = processBounds();
		Expression c = null;
		for (Expression e : newConjuncts) {
			Operation o = (Operation) e;
			if (o.getOperand(1) == Operation.ZERO) {
				if (o.getOperator() == Operation.Operator.GT) {
					e = new Operation(Operation.Operator.GE, merge(o.getOperand(0), new IntConstant(-1)),
							o.getOperand(1));
				} else if (o.getOperator() == Operation.Operator.LT) {
					e = new Operation(Operation.Operator.LE, merge(o.getOperand(0), new IntConstant(1)),
							o.getOperand(1));
				}
			}
			o = (Operation) e;
			// added by jia xiangyang
			if (isFirstExpNegtive(o)) {
				Operator operator = null;
				if (o.getOperator() == Operation.Operator.GE) {
					operator = Operation.Operator.LE;
				} else if (o.getOperator() == Operation.Operator.LE) {
					operator = Operation.Operator.GE;
				} else if (o.getOperator() == Operation.Operator.EQ || o.getOperator() == Operation.Operator.NE) {
					operator = o.getOperator();
				}
				if (operator != null) {
					e = new Operation(operator, scale(-1, o.getOperand(0)), o.getOperand(1));
				}
			}

			if (c == null) {
				c = e;
			} else {
				c = new Operation(Operation.Operator.AND, c, e);
			}
		}
		return (c == null) ? Operation.TRUE : c;
	}

	private boolean isFirstExpNegtive(Operation ex) {
		Operation ex2 = ex;
		while (ex2 != null && ex2.getOperator() != Operation.Operator.MUL) {
			Expression op0 = ex2.getOperand(0);
			if (op0 instanceof Operation) {
				ex2 = (Operation) op0;
			} else {
				ex2 = null;
				break;
			}
		}
		boolean isFirstExpNegtive = false;
		if (ex2 != null && ex2.getOperator() == Operation.Operator.MUL) {
			Expression op0 = ex2.getOperand(0);
			if (op0 instanceof Constant && NumberUtil.getValue((Constant) op0).doubleValue() < 0.0) {
				isFirstExpNegtive = true;
			}
		}
		return isFirstExpNegtive;
	}

	private SortedSet<Expression> processBounds() {
		return conjuncts;
	}

	@Override
	public void postVisit(Constant constant) {
		if (!unsatisfiable) {
			if (constant instanceof IntConstant) {
				stack.push(constant);
			} else if (constant instanceof RealConstant) {
				stack.push(constant);
			} else {
				stack.clear();
			}
		}
	}

	@Override
	public void postVisit(Variable variable) {
		if (!unsatisfiable) {
			if (variable instanceof IntVariable) {
				variableSet.add(variable);
				stack.push(new Operation(Operation.Operator.MUL, Operation.ONE, variable));
			} else if (variable instanceof RealVariable) {
				variableSet.add(variable);
				stack.push(new Operation(Operation.Operator.MUL, NumberUtil.getConstant(1.0), variable));
			} else {
				stack.clear();
			}
		}
	}

	@Override
	public void postVisit(Operation operation) throws VisitorException {
		if (unsatisfiable) {
			return;
		}
		Operation.Operator op = operation.getOperator();
		int arity = op.getArity();
		Expression r = stack.pop();
		Expression l = arity > 1&&!stack.isEmpty() ? stack.pop() : null;
		switch (op) {
		case AND:
			if (r!=null&&!Operation.TRUE.equals(r)) {
				conjuncts.add(r);
			}
			if (l!=null&&!Operation.TRUE.equals(l)) {
				conjuncts.add(l);
			}
			break;
		case ITE:
			Operation c = (Operation) stack.pop();
			stack.push(new Operation(op, c, l, r));
			break;
		case EQ:
		case NE:
		case LT:
		case LE:
		case GT:
		case GE:
			if (r instanceof Operation && ((Operation) r).getOperator() == Operator.ITE) {
				break;
			}
			pushCompareExpression(op, r, l);
			break;
		case ADD:
			stack.push(merge(r, l));
			break;
		case SUB:
			stack.push(merge(scale(-1, r), l));
			break;
		case MUL:
			if ((l instanceof Constant) && (r instanceof Constant)) {
				Number li = NumberUtil.getValue((Constant) l);
				Number ri = NumberUtil.getValue((Constant) r);
				stack.push(NumberUtil.getConstant(NumberUtil.multiply(li, ri)));
			} else if (l instanceof Constant) {
				Number li = NumberUtil.getValue((Constant) l);
				stack.push(scale(li, r));
			} else if (r instanceof Constant) {
				Number ri = NumberUtil.getValue((Constant) r);
				stack.push(scale(ri, l));
			} else if (r instanceof Operation && l instanceof Operation) {
				Operation or = ((Operation) r);
				Operation ol = ((Operation) l);
				Number coff = 1;
				Expression ex;
				if (or.getOperator().equals(Operator.MUL) && or.getOperand(0) instanceof Constant) {
					coff = NumberUtil.getValue((Constant) or.getOperand(0));
					ex = or.getOperand(1);
				} else {
					ex = or;
				}
				if (ol.getOperator().equals(Operator.MUL) && ol.getOperand(0) instanceof Constant) {
					coff = NumberUtil.multiply(coff, NumberUtil.getValue((Constant) ol.getOperand(0)));
					ex = new Operation(Operator.MUL, ex, ol.getOperand(1));
				} else {
					ex = new Operation(Operator.MUL, ex, ol);
				}

				stack.push(new Operation(Operator.MUL, NumberUtil.getConstant(coff), ex));
				// stack.clear();
				// linearInteger = false;
			}
			break;
		case DIV:
			if ((l instanceof Constant) && (r instanceof Constant)) {
				Number li = NumberUtil.getValue((Constant) l);
				Number ri = NumberUtil.getValue((Constant) r);
				stack.push(NumberUtil.getConstant(NumberUtil.div(li, ri)));
			} else if (l instanceof Constant) {
				Number li = NumberUtil.getValue((Constant) l);
				stack.push(scale(li, r));
			} else if (r instanceof Constant) {
				Number ri = NumberUtil.getValue((Constant) r);
				stack.push(scale(NumberUtil.div(1, ri), l));
			} else {
				Operation or = ((Operation) r);
				Operation ol = ((Operation) l);
				Number coff = 1;
				Expression ex;
				
				if (ol.getOperator().equals(Operator.MUL) && ol.getOperand(0) instanceof Constant) {
					coff = NumberUtil.getValue((Constant) ol.getOperand(0));
					ex = ol.getOperand(1);
				} else {
					ex = ol;
				}
				if (or.getOperator().equals(Operator.MUL) && or.getOperand(0) instanceof Constant) {
					coff = NumberUtil.div(coff, NumberUtil.getValue((Constant)or.getOperand(0)));
					ex = new Operation(Operator.DIV,ex,or.getOperand(1));
				} else {
					ex = new Operation(Operator.DIV,ex,or );
				}
				stack.push(new Operation(Operator.MUL, NumberUtil.getConstant(coff), ex));
				// stack.clear();
				// linearInteger = false;
			}
			break;
		case SIN:
		case COS:
		case ABS:
		case SQRT:
		case TAN:
		case ASIN:
		case ACOS:
		case ROUND:
		case ATAN:
		case POWER:
		case LOG:
			Operation o = new Operation(op, r);
			stack.push(new Operation(Operation.Operator.MUL, NumberUtil.getConstant(1.0), o));
			break;
		default:
			break;
		}
	}

	private void pushCompareExpression(Operation.Operator op, Expression r, Expression l) {
		Expression e = merge(scale(-1, r), l);
		if (e instanceof Constant) {
			boolean b = compareWithZero(op, (Constant) e);
			if (b) {
				stack.push(Operation.TRUE);
			} else {
				unsatisfiable = true;
			}
		} else {
			Expression zero = NumberUtil.containtReal(e) ? Operation.REAL_ZERO : Operation.ZERO;
			stack.push(new Operation(op,e,zero));
		}
	}

	private boolean compareWithZero(Operation.Operator op, Constant e) {
		double dif = NumberUtil.getValue(e).doubleValue();
		boolean b = true;
		if (op == Operation.Operator.EQ) {
			b = dif == 0.0;
		} else if (op == Operation.Operator.NE) {
			b = dif != 0.0;
		} else if (op == Operation.Operator.LT) {
			b = dif < 0.0;
		} else if (op == Operation.Operator.LE) {
			b = dif <= 0.0;
		} else if (op == Operation.Operator.GT) {
			b = dif > 0.0;
		} else if (op == Operation.Operator.GE) {
			b = dif >= 0.0;
		}
		return b;
	}

	private Expression merge(Expression left, Expression right) {
		Map<Expression, Number> coefficients = new TreeMap<Expression, Number>();
		putIntoCoefficients(left,coefficients);
		putIntoCoefficients(right,coefficients);
		Expression result = buildOperation(coefficients);
		return result;
	}
	
	//array初始化为ArrayType的变量，index初始化为IntegerType的变量
	boolean isPositive(int[] array, int index){ 
	    if(array!=null){  //需要进行对象比较
		if(index<array.length){ //需要将数组长度符号化表示为一元表达式
			if(array[index]>0){ //需要将数组下标符号化表示为二元表达式
				return true;
			}else{
				return false;
			}
		    } 
		}
	     throw new IllegalArgumentException("invalid array index.");
	}
	
	private void putIntoCoefficients(Expression exp,Map<Expression, Number> coeff) {
		if (exp instanceof Constant) {
			addCoefficient(coeff, NumberUtil.ONE, NumberUtil.getValue((Constant) exp));
			return;
		}
		if (exp instanceof Variable){
			addCoefficient(coeff, exp, 1);
			return;
		}
		if (exp instanceof Operation){
			Operation oexp=(Operation)exp;
			if(oexp.getOperator()==Operation.Operator.MUL){
				Number c=NumberUtil.getValue((Constant)oexp.getOperand(0));
				addCoefficient(coeff, oexp.getOperand(1), c);
				return;
			}
			if(oexp.getOperator()==Operation.Operator.ADD){
				putIntoCoefficients(oexp.getOperand(0),coeff);
				putIntoCoefficients(oexp.getOperand(1),coeff);
				return;
			}
			throw new IllegalArgumentException("Bug: cannnot put into Coefficients:"+exp);
		}
	}

	private void addCoefficient(Map<Expression, Number> coeff,  Expression e,Number s) {
		Number n=coeff.get(e);
		n=(n==null)?s:NumberUtil.add(s,n);
		coeff.put(e, n);
	}

	private Expression buildOperation(Map<Expression, Number> coefficients) {
		Expression lr = null;
		Number constant=0;
		for (Map.Entry<Expression, Number> e : coefficients.entrySet()) {
			Number coef = e.getValue();
			if (coef.doubleValue() != 0.0) {
				if(NumberUtil.ONE.equals(e.getKey())){
					constant=coef;
					continue;
				}
				Operation term = new Operation(Operation.Operator.MUL, NumberUtil.getConstant(coef), e.getKey());
				if (lr == null) {
					lr = term;
				} else {
					lr = new Operation(Operation.Operator.ADD, lr, term);
				}
			}
		}
		if(lr==null){
			lr=NumberUtil.getConstant(constant);
		}else if(!constant.equals(0)){
			lr = new Operation(Operation.Operator.ADD, lr, NumberUtil.getConstant(constant));
		}
		return lr;
	}

	// private Expression buildOperation(Map<Expression, Number> coefficients) {
	// Expression lr = null;
	// int size=coefficients.size();
	// for(int i=0;i<size;i++){
	// Map.Entry<Expression, Number> max=null;
	// for (Map.Entry<Expression, Number> e : coefficients.entrySet()) {
	// if(max==null){
	// max=e;
	// }else {
	// float d=NumberUtil.sub(e.getValue(),max.getValue()).floatValue();
	// if(d>0.0){
	// max=e;
	// }else if(d==0&&e.getKey().compareTo(max.getKey())>0){
	// max=e;
	// }
	// }
	// }
	//
	// Number coef = max.getValue();
	// if (coef.doubleValue() != 0.0) {
	// Operation term = new Operation(Operation.Operator.MUL,
	// NumberUtil.getConstant(coef), max.getKey());
	// if (lr == null) {
	// lr = term;
	// } else {
	// lr = new Operation(Operation.Operator.ADD, lr, term);
	// }
	// }
	// coefficients.remove(max.getKey());
	// }
	////
	//
	//// //coefficients.remove(e);
	//// }
	// return lr;
	// }
	//

	private boolean hasRightConstant(Expression expression) {
		return isAddition(expression) && (getRightExpression(expression) instanceof Constant);
	}

	private Number getRightConstant(Expression expression) {
		return NumberUtil.getValue((Constant) getRightExpression(expression));
	}

	private Expression getLeftExpression(Expression expression) {
		return ((Operation) expression).getOperand(0);
	}

	private Expression getRightExpression(Expression expression) {
		return ((Operation) expression).getOperand(1);
	}

	private Operation getLeftOperation(Expression expression) {
		return (Operation) getLeftExpression(expression);
	}

	private boolean isAddition(Expression expression) {
		return Operation.Operator.ADD == ((Operation) expression).getOperator();
	}

	private Expression scale(Number factor, Expression expression) {
		if (factor.doubleValue() == 0.0) {
			return Operation.ZERO;
		}
		if (expression instanceof Constant) {
			return NumberUtil.getConstant(NumberUtil.multiply(factor, NumberUtil.getValue((Constant) expression)));
		} else if (expression instanceof Variable) {
			return expression;// TODO is it right?????
		} else {
			assert (expression instanceof Operation);
			Operation o = (Operation) expression;
			Operation.Operator p = o.getOperator();
			List<Expression> exps = new ArrayList<Expression>();
			Iterator<Expression> itr = o.getOperands().iterator();
			while (itr.hasNext()) {
				Expression e = scale(factor, itr.next());
				exps.add(e);
			}
			return new Operation(p, exps.toArray(new Expression[0]));
		}
	}

}