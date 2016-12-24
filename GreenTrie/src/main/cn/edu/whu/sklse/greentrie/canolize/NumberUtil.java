package cn.edu.whu.sklse.greentrie.canolize;

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;

import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;

public class NumberUtil {
	
	public static final IntConstant ONE=(IntConstant)NumberUtil.getConstant(1);
	public static final IntConstant ZERO=(IntConstant)NumberUtil.getConstant(0);
	
	private NumberUtil(){
		
	}

	public static  Number add(Number a, Number b) {
		
		if (a instanceof Integer && b instanceof Integer) {
			return (Integer)a + (Integer)b;
		}
		try {
			Fraction av = (a instanceof Fraction) ? (Fraction) a : new Fraction(a.doubleValue());
			Fraction bv = (b instanceof Fraction) ? (Fraction) b : new Fraction(b.doubleValue());
			return av.add(bv);
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot add two number:" + a + "," + b,e);
		}
		
	}
	
	public static boolean isZero(Number b){
		if(b instanceof Integer) {
			return b.intValue()==0;
		}
		if(b instanceof Double){
			return b.equals(0.0);
		}
		throw new IllegalArgumentException("unsurported type for isZero Function:" +b.getClass());
		
	}

	public static  Number multiply(Number a, Number b) {
		if (a instanceof Integer && b instanceof Integer) {
			return a.intValue() * b.intValue();
		}

		if (a.equals(Double.POSITIVE_INFINITY) || b.equals(Double.POSITIVE_INFINITY)
				|| a.equals(Double.NEGATIVE_INFINITY) || b.equals(Double.NEGATIVE_INFINITY)) {
			int v1 = compare(a, 0);
			int v2 = compare(b, 0);
			int sig = v1 * v2;
			if (sig > 0) {
				return Double.POSITIVE_INFINITY;
			} else if (sig == 0) {
				return 0.0;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}

		try {
			Fraction av = (a instanceof Fraction) ? (Fraction) a : new Fraction(a.doubleValue());
			Fraction bv = (b instanceof Fraction) ? (Fraction) b : new Fraction(b.doubleValue());
			return av.multiply(bv);
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot multiply two number:" + a + "," + b,e);
		}
		
	}

	public static  Number div(Number a, Number b) {
		if (a instanceof Integer && b instanceof Integer) {
			return a.intValue() / b.intValue();
		}
		try {
			Fraction av = (a instanceof Fraction) ? (Fraction) a : new Fraction(a.doubleValue());
			Fraction bv = (b instanceof Fraction) ? (Fraction) b : new Fraction(b.doubleValue());
			return av.divide(bv);
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot divide two number:" + a + "," + b,e);
		}
		
	}

	public static  int compare(Number a, Number b) {
		if (a.equals(b)) {
			return 0;
		}
		if (a.equals(Double.NEGATIVE_INFINITY) || b.equals(Double.POSITIVE_INFINITY)) {
			return -1;
		} else if (a.equals(Double.POSITIVE_INFINITY) || b.equals(Double.NEGATIVE_INFINITY)) {
			return +1;
		}
		return Double.compare(a.doubleValue(),b.doubleValue());
	}

	public static  Number sub(Number a, Number b) {
		if (a instanceof Integer && b instanceof Integer) {
			return a.intValue() - b.intValue();
		}
		try {
			Fraction av = (a instanceof Fraction) ? (Fraction) a : new Fraction(a.doubleValue());
			Fraction bv = (b instanceof Fraction) ? (Fraction) b : new Fraction(b.doubleValue());
			return av.subtract(bv);
		} catch (FractionConversionException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("cannot subtract two number:" + a + "," + b);
	}

	public static  Number getValue(Constant con) {
		if (con instanceof IntConstant) {
			return ((IntConstant) con).getValue();
		} else if (con instanceof RealConstant) {
			return ((RealConstant) con).getFractionValue();
		} else {
			return null;
		}
	}

	public static  Constant getConstant(Number num) {
		if (num instanceof Integer) {
			return new IntConstant(num.intValue());
		} else if (num instanceof Double) {
			return new RealConstant(num.doubleValue());
		} else if (num instanceof Fraction){
			return new RealConstant((Fraction)num);
		}else{
			throw new IllegalArgumentException("cannot create constant for "+num);
		}
	}

	public static  boolean containtReal(Expression exp) {
		if (exp instanceof RealConstant || exp instanceof RealVariable) {
			return true;
		}
		if (exp instanceof Operation) {
			for (Expression e : ((Operation) exp).getOperands()) {
				if (containtReal(e)) {
					return true;
				}
			}
		}
		return false;
	}

}
