package za.ac.sun.cs.green.expr;

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class RealConstant extends Constant {

	//private final double value;
	
	private Fraction value; //modified by Jia

	@JsonCreator 
	public RealConstant(@JsonProperty("value")final double value) {
		this.value=null;
		try {
			this.value=new Fraction(value);
		} catch (FractionConversionException e) {
			e.printStackTrace();
		}
	}
	
	public RealConstant(final Fraction value) {
		this.value=value;
	}

	public final double getValue() {
		return value.doubleValue();
	}

	public final Fraction getFractionValue() {
		return value;
	}
	
	@Override
	public void accept(Visitor visitor) throws VisitorException {
		visitor.preVisit(this);
		visitor.postVisit(this);
	}

//	@Override
//	public int compareTo(Expression expression) {
//		RealConstant constant = (RealConstant) expression;
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
		if (object instanceof RealConstant) {
			RealConstant constant = (RealConstant) object;
			return this.getFractionValue().equals(constant.getFractionValue());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		if(value==null){
			return "null";
		}else{
			return ""+this.getValue();
		}
	}

}
