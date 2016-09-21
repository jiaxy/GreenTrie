package cn.edu.whu.sklse.greentrie.canolize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import choco.cp.model.managers.operators.SumManager;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.util.Reporter;
import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

public class SATCanonizerService extends BasicService {

	/**
	 * Number of times the slicer has been invoked.
	 */
	private int invocations = 0;

	public SATCanonizerService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(getClass());
		if (result == null) {
			final Map<Variable, Variable> map = new HashMap<Variable, Variable>();
			final Expression e = canonize(instance.getFullExpression(), map);
			final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
			result = Collections.singleton(i);
			instance.setData(getClass(), result);
		}
		return result;
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocations = " + invocations);
	}

	public Expression canonize(Expression expression, Map<Variable, Variable> map) {
		try {
			invocations++;
			OrderingVisitor orderingVisitor = new OrderingVisitor();
			expression.accept(orderingVisitor);
			Expression expression2 = orderingVisitor.getExpression();
			if(!expression.equals(expression2)){
				System.out.println("expression:"+expression);
				System.out.println("expression after order:"+expression2);
			}
			expression = expression2;
			CanonizationVisitor canonizationVisitor = new CanonizationVisitor();
			expression.accept(canonizationVisitor);
			//Collections.sort(list);
			Expression canonized = canonizationVisitor.getExpression();
//			System.out.println("canonized:" + canonized);
			canonized = new Reducer().reduce(canonized);
//			System.out.println("reduced:" + canonized);
			if (canonized != null) {
				canonized = new Renamer(map, canonizationVisitor.getVariableSet()).rename(canonized);
			}
			return canonized;
		} catch (VisitorException x) {
			log.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		return null;
	}

	private static class OrderingVisitor extends Visitor {

		private Stack<Expression> stack;

		public OrderingVisitor() {
			stack = new Stack<Expression>();
		}

		public Expression getExpression() {
			return stack.pop();
		}

		@Override
		public void postVisit(IntConstant constant) {
			stack.push(constant);
		}

		@Override
		public void postVisit(IntVariable variable) {
			stack.push(variable);
		}

		@Override
		public void postVisit(RealConstant constant) {
			stack.push(constant);
		}

		@Override
		public void postVisit(RealVariable variable) {
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

	private static class CanonizationVisitor extends Visitor {

		private Stack<Expression> stack;

		private SortedSet<Expression> conjuncts;

		private SortedSet<Variable> variableSet;

		private Map<Variable, Integer> lowerBounds;

		private Map<Variable, Integer> upperBounds;

		private IntVariable boundVariable;

		private Integer bound;

		private int boundCoeff;

		private boolean unsatisfiable;

		private boolean linearInteger;

		public CanonizationVisitor() {
			stack = new Stack<Expression>();
			conjuncts = new TreeSet<Expression>();
			variableSet = new TreeSet<Variable>();
			unsatisfiable = false;
			linearInteger = true;
		}

		public SortedSet<Variable> getVariableSet() {
			return variableSet;
		}

		public Expression getExpression() {
			if (!linearInteger) {
				return null;
			} else if (unsatisfiable) {
				return Operation.FALSE;
			} else {
				if (!stack.isEmpty()) {
					Expression x = stack.pop();
					if (!x.equals(Operation.TRUE)) {
						conjuncts.add(x);
					}
				}
				SortedSet<Expression> newConjuncts = processBounds();
				// new TreeSet<Expression>();
				Expression c = null;
				for (Expression e : newConjuncts) {
					Operation o = (Operation) e;
					if(o.getOperand(1)==Operation.ZERO){
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
		}
		
		private boolean isFirstExpNegtive(Operation ex) {
			while (ex != null && ex.getOperator() != Operation.Operator.MUL) {
				Expression op0 = ex.getOperand(0);
				if (op0 instanceof Operation) {
					ex = (Operation) op0;
				} else {
					ex = null;
					break;
				}
			}
			boolean isFirstExpNegtive = false;
			if (ex != null && ex.getOperator() == Operation.Operator.MUL) {
				Expression op0 = ex.getOperand(0);
				if (op0 instanceof Constant) {
					if (NumberUtil.getValue((Constant) op0).doubleValue() < 0.0) {
						isFirstExpNegtive = true;
					}
				}
			}
			return isFirstExpNegtive;
		}

		private SortedSet<Expression> processBounds() {
			return conjuncts;
		}


		@Override
		public void postVisit(Constant constant) {
			if (linearInteger && !unsatisfiable) {
				if (constant instanceof IntConstant) {
					stack.push(constant);
				} else if (constant instanceof RealConstant) {
					stack.push(constant);
				} else {
					stack.clear();
					linearInteger = false;
				}
			}
		}

		@Override
		public void postVisit(Variable variable) {
			if (linearInteger && !unsatisfiable) {
				if (variable instanceof IntVariable) {
					variableSet.add(variable);
					stack.push(new Operation(Operation.Operator.MUL, Operation.ONE, variable));
				}else if (variable instanceof RealVariable) {
					variableSet.add(variable);
					stack.push(new Operation(Operation.Operator.MUL, NumberUtil.getConstant(1.0), variable));
				} else {
					stack.clear();
					linearInteger = false;
				}
			}
		}
		

		@Override
		public void postVisit(Operation operation) throws VisitorException {
			if (!linearInteger || unsatisfiable) {
				return;
			}
			Operation.Operator op = operation.getOperator();
			Expression r = null;
			Expression l = null;
			switch (op) {
			case AND:
				if (!stack.isEmpty()) {
					Expression x = stack.pop();
					if (!x.equals(Operation.TRUE)) {
						conjuncts.add(x);
					}
				}
				if (!stack.isEmpty()) {
					Expression x = stack.pop();
					if (!x.equals(Operation.TRUE)) {
						conjuncts.add(x);
					}
				}
				break;
			case ITE:
				r = stack.pop();
				l = stack.pop();
				Operation c = (Operation) stack.pop();
				stack.push(new Operation(op, c, l, r));
				break;
			case EQ:
			case NE:
			case LT:
			case LE:
			case GT:
			case GE:
				r = stack.pop();
				l = stack.pop();
				if (r instanceof Operation && ((Operation) r).getOperator() == Operator.ITE) {
					break;
				}
				Expression e = merge(scale(-1, r), l);
				if (e instanceof Constant) {
					boolean b = compareWithZero(op, (Constant) e);
					if (b) {
						stack.push(Operation.TRUE);
					} else {
						unsatisfiable = true;
					}
				} else {
					stack.push(createCompareExpression(op, e));
				}
				break;
			case ADD:
				stack.push(merge(stack.pop(), stack.pop()));
				break;
			case SUB:
				stack.push(merge(scale(-1, stack.pop()), stack.pop()));
				break;
			case MUL:
				r = stack.pop();
				l = stack.pop();
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
				} else if(r instanceof Operation && l instanceof Operation){
					Operation or = ((Operation)r);
					Operation ol = ((Operation)l);
					Number coff=1;
					Expression ex;
					if(or.getOperator().equals(Operator.MUL)&& or.getOperand(0) instanceof Constant){
							coff=NumberUtil.getValue((Constant) or.getOperand(0));
							ex=or.getOperand(1);
					}else{
						ex=or;
					}
					if(ol.getOperator().equals(Operator.MUL)&& ol.getOperand(0) instanceof Constant){
							coff=NumberUtil.multiply(coff,NumberUtil.getValue((Constant) or.getOperand(0)));
							ex=new Operation(Operator.MUL,ex,or.getOperand(1));
					}else{
						ex=new Operation(Operator.MUL,ex,or);
					}
					
					stack.push(new Operation(Operator.MUL,NumberUtil.getConstant(coff),ex));
//					stack.clear();
//					linearInteger = false;
				}
				break;
			case DIV:
				r = stack.pop();
				l = stack.pop();
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
					stack.push(new Operation(Operator.MUL,l,r));
//					stack.clear();
//					linearInteger = false;
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
				Operation o=new Operation(op,stack.pop());
				stack.push(new Operation(Operation.Operator.MUL, NumberUtil.getConstant(1.0), o));
				
				break;
			default:
				break;
			}
		}

		private Operation createCompareExpression(Operation.Operator op, Expression e) {
			Expression zero=(NumberUtil.containtReal(e))?Operation.REAL_ZERO:Operation.ZERO;
			return new Operation(op, e, zero);
		}

		private boolean compareWithZero(Operation.Operator op, Constant e) {
			Number v = 0;
			if (e instanceof IntConstant) {
				v = ((IntConstant) e).getValue();
			} else if (e instanceof RealConstant) {
				v = ((RealConstant) e).getValue();
			}
			double dif = v.doubleValue();

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
			Operation l = null;
			Operation r = null;
			Number s = 0;
			if (left instanceof Constant) {
				s = NumberUtil.getValue((Constant) left);
			} else {
				if (hasRightConstant(left)) {
					s = getRightConstant(left);
					l = getLeftOperation(left);
				} else {
					l = (Operation) left;
				}
			}
			if (right instanceof Constant) {
				s = NumberUtil.add(s, NumberUtil.getValue((Constant) right));
				// s += ((IntConstant) right).getValue();
			} else {
				if (hasRightConstant(right)) {
					s = NumberUtil.add(s, getRightConstant(right));
					// s += getRightConstant(right);
					r = getLeftOperation(right);
				} else {
					r = (Operation) right;
				}
			}
			Map<Expression, Number> coefficients = new TreeMap<Expression, Number>();
			Constant c;
			Expression v;
			Number k;

			// Collect the coefficients of l
			if (l != null) {
				while (l.getOperator() == Operation.Operator.ADD) {
					Operation o = (Operation) l.getOperand(1);
					assert (o.getOperator() == Operation.Operator.MUL);
					c = (Constant) o.getOperand(0);
					v = (Expression) o.getOperand(1);
					coefficients.put(v, NumberUtil.getValue(c));
					l = (Operation) l.getOperand(0);
				}
				assert (l.getOperator() == Operation.Operator.MUL);
				c = (Constant) l.getOperand(0);
				v = (Expression) l.getOperand(1);
				coefficients.put(v, NumberUtil.getValue(c));
			}

			// Collect the coefficients of r
			if (r != null) {
				while (r.getOperator() == Operation.Operator.ADD) {
					Operation o = (Operation) r.getOperand(1);
					assert (o.getOperator() == Operation.Operator.MUL);
					c = (Constant) o.getOperand(0);
					v = (Expression) o.getOperand(1);
					k = coefficients.get(v);
					if (k == null) {
						coefficients.put(v, NumberUtil.getValue(c));
					} else {
						coefficients.put(v, NumberUtil.add(NumberUtil.getValue(c), k));
					}
					r = (Operation) r.getOperand(0);
				}
				assert (r.getOperator() == Operation.Operator.MUL);
				c = (Constant) r.getOperand(0);
				v = (Expression) r.getOperand(1);
				k = coefficients.get(v);
				if (k == null) {
					coefficients.put(v, NumberUtil.getValue(c));
				} else {
					coefficients.put(v, NumberUtil.add(NumberUtil.getValue(c), k));
				}
			}
			Expression result = buildOperation(coefficients);
//			if(result==null||!result.equals(result2)){
//				System.out.println("merging result is diffrent:");
//				System.out.println("left:"+left);
//				System.out.println("rigth:"+right);
//				System.out.println("result 1:"+result);
//				System.out.println("result 2:"+result2);
//			}
			if ((result == null) || (result instanceof Constant)) {
				return NumberUtil.getConstant(s);
			} else if (s.doubleValue()!= 0.0) {
				result= new Operation(Operation.Operator.ADD, result, NumberUtil.getConstant(s));
			}
//			System.out.println("merge:"+left+" and"+right);
//			System.out.println("result:"+result);
			return result;
		}
		
		private Expression buildOperation(Map<Expression, Number> coefficients) {
			Expression lr = null;
			for(Map.Entry<Expression, Number> e:coefficients.entrySet()){
				Number coef = e.getValue();
				if (coef.doubleValue() != 0.0) {
					Operation term = new Operation(Operation.Operator.MUL, NumberUtil.getConstant(coef), e.getKey());
					if (lr == null) {
						lr = term;
					} else {
						lr = new Operation(Operation.Operator.ADD, lr, term);
					}
				}
			}
			return lr;
		}

//		private Expression buildOperation(Map<Expression, Number> coefficients) {
//			Expression lr = null;
//			int size=coefficients.size();
//			for(int i=0;i<size;i++){
//				Map.Entry<Expression, Number> max=null;
//				for (Map.Entry<Expression, Number> e : coefficients.entrySet()) {
//					if(max==null){
//						max=e;
//					}else {
//						float d=NumberUtil.sub(e.getValue(),max.getValue()).floatValue();
//						if(d>0.0){
//							max=e;
//						}else if(d==0&&e.getKey().compareTo(max.getKey())>0){
//							max=e;
//						}
//					}
//				}
//
//				Number coef = max.getValue();
//				if (coef.doubleValue() != 0.0) {
//					Operation term = new Operation(Operation.Operator.MUL, NumberUtil.getConstant(coef), max.getKey());
//					if (lr == null) {
//						lr = term;
//					} else {
//						lr = new Operation(Operation.Operator.ADD, lr, term);
//					}
//				}
//				coefficients.remove(max.getKey());
//			}
////			
//
////				//coefficients.remove(e);
////			}
//			return lr;
//		}
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
			return Operation.Operator.ADD==((Operation) expression).getOperator();
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

//	private static class Renamer extends Visitor {
//
//		private Map<Variable, Variable> map;
//
//		private Stack<Expression> stack;
//
//		public Renamer(Map<Variable, Variable> map, SortedSet<Variable> variableSet) {
//			this.map = map;
//			stack = new Stack<Expression>();
//		}
//
//		public Expression rename(Expression expression) throws VisitorException {
//			expression.accept(this);
//			return stack.pop();
//		}
//
//		@Override
//		public void postVisit(IntVariable variable) {
//			Variable v = map.get(variable);
//			if (v == null) {
//				v = new IntVariable("v" + map.size(), variable.getLowerBound(), variable.getUpperBound());
//				map.put(variable, v);
//			}
//			stack.push(v);
//		}
//		
//		@Override
//		public void postVisit(RealVariable variable) {
//			Variable v = map.get(variable);
//			if (v == null) {
//				v = new RealVariable("v" + map.size(), variable.getLowerBound(), variable.getUpperBound());
//				map.put(variable, v);
//			}
//			stack.push(v);
//		}
//
//		@Override
//		public void postVisit(Constant constant) {
//			stack.push(constant);
//		}
//
//		@Override
//		public void postVisit(Operation operation) {
//			int arity = operation.getOperator().getArity();
//			Expression operands[] = new Expression[arity];
//			for (int i = arity; i > 0; i--) {
//				operands[i - 1] = stack.pop();
//			}
//			stack.push(new Operation(operation.getOperator(), operands));
//		}
//
//	}

}
