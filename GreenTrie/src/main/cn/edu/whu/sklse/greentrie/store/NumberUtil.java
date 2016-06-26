package cn.edu.whu.sklse.greentrie.store;

public class NumberUtil {
	

	static public Number add(Number a, Number b) {
		Number result = 0;
		if (a instanceof Double || b instanceof Double) {
			result = a.doubleValue() + b.doubleValue();
		}else if (a instanceof Float || b instanceof Float) {
			result = a.floatValue() + b.floatValue();
		}else if (a instanceof Long || b instanceof Long) {
			result = a.longValue() + b.longValue();
		}else if (a instanceof Integer && b instanceof Integer) {
			result = a.intValue() + b.intValue();
		} else {
			throw new RuntimeException("cannot add two number:" + a + "," + b);
		}
		return result;
	}

	static public Number multiply(Number a, Number b) {
		Number result = 1;
		if (a instanceof Double || b instanceof Double) {
			result = a.doubleValue() * b.doubleValue();
		}else if (a instanceof Float || b instanceof Float) {
			result = a.floatValue() * b.floatValue();
		}else if (a instanceof Long || b instanceof Long) {
			result = a.longValue() * b.longValue();
		}else if (a instanceof Integer && b instanceof Integer) {
			result = a.intValue() * b.intValue();
		} else {
			throw new RuntimeException("cannot add two number:" + a + "," + b);
		}
		if (result.equals(-0.0)) {
			result = 0.0;
		}
		return result;
	}

	static public Number div(Number a, Number b) {
		Number result = 1;
		if (a instanceof Double || b instanceof Double) {
			result = a.doubleValue() / b.doubleValue();
		}else if (a instanceof Float || b instanceof Float) {
			result = a.floatValue() / b.floatValue();
		}else if (a instanceof Long || b instanceof Long) {
			result = a.longValue() / b.longValue();
		}else if (a instanceof Integer && b instanceof Integer) {
			result = a.intValue() / b.intValue();
		} else {
			throw new RuntimeException("cannot add two number:" + a + "," + b);
		}
		if (result.equals(-0.0)) {
			result = 0.0;
		}
		return result;
	}

	static public Number sub(Number a, Number b) {
		Number result = 0;
		if (a instanceof Double || b instanceof Double) {
			result = a.doubleValue() - b.doubleValue();
		}else if (a instanceof Float || b instanceof Float) {
			result = a.floatValue() - b.floatValue();
		}else if (a instanceof Long || b instanceof Long) {
			result = a.longValue() - b.longValue();
		}else if (a instanceof Integer && b instanceof Integer) {
			result = a.intValue() - b.intValue();
		} else {
			throw new RuntimeException("cannot add two number:" + a + "," + b);
		}
		return result;
	}



}
