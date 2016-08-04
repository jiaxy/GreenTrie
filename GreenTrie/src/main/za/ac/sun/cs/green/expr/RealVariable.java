package za.ac.sun.cs.green.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RealVariable extends Variable {

	private final Double lowerBound;

	private final Double upperBound;

	public RealVariable(String name, Object original, Double lowerBound, Double upperBound) {
		super(name, original);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	@JsonCreator 
	public RealVariable(@JsonProperty("name")String name, @JsonProperty("lowerBound")Double lowerBound, @JsonProperty("upperBound")Double upperBound) {
		super(name);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public Double getUpperBound() {
		return upperBound;
	}

	@Override
	public void accept(Visitor visitor) throws VisitorException {
		visitor.preVisit(this);
		visitor.postVisit(this);
	}

//	@Override
//	public int compareTo(Expression expression) {
//		RealVariable variable = (RealVariable) expression;
//		return getName().compareTo(variable.getName());
//	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof RealVariable) {
			RealVariable variable = (RealVariable) object;
			return getName().equals(variable.getName());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

}
