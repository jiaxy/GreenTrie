package za.ac.sun.cs.green.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntConstant extends Constant {

	private final int value;

	@JsonCreator 
	public IntConstant(@JsonProperty("value")final int value) {
		this.value = value;
	}

	public final int getValue() {
		return value;
	}

	@Override
	public void accept(Visitor visitor) throws VisitorException {
		visitor.preVisit(this);
		visitor.postVisit(this);
	}

//	@Override
//	public int compareTo(Expression expression) {
//		IntConstant constant = (IntConstant) expression;
//		if (value < constant.value) {
//			return -1;
//		} else if (value > constant.value) {
//			return 1;
//		} else {
//			return 1;
//		}
//	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof IntConstant) {
			IntConstant constant = (IntConstant) object;
			return value == constant.value;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
