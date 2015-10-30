package cn.edu.whu.sklse.greentrie.canolize;

import za.ac.sun.cs.green.expr.Constant;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;

public class NumberUtil {

	static public Number add(Number a, Number b) {
		Number result = 0;
		if (a instanceof Integer) {
			if (b instanceof Integer) {
				result = a.intValue() + b.intValue();
			} else {
				result = a.intValue() + b.doubleValue();
			}
		} else {
			result = a.doubleValue() + b.doubleValue();
		}
		return result;
	}

	static public Number multiply(Number a, Number b) {
		Number result = 1;
		if (a instanceof Integer) {
			if (b instanceof Integer) {
				result = a.intValue() * b.intValue();
			} else {
				result = a.intValue() * b.doubleValue();
			}
		} else {
			result = a.doubleValue() * b.doubleValue();
		}
		if(result.equals(-0.0)) {
			result=0.0;
		}
		return result;
	}
	
	static public double sub(Number a, Number b) {
		return a.doubleValue()-b.doubleValue();
	}

	static public Number getValue(Constant con){
		if(con instanceof IntConstant){
			return ((IntConstant) con).getValue();
		}else if(con instanceof RealConstant){
			return ((RealConstant) con).getValue();
		}else{
			return null;
		}
	}
	
	static public Constant getConstant(Number num){
		if(num instanceof Integer){
			return new IntConstant(num.intValue());
		}else if(num instanceof Double){
			return new RealConstant(num.doubleValue());
		}else{
			return null;
		}
	}
	
	static public boolean containtReal(Expression exp) {
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
