package za.ac.sun.cs.green.expr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public abstract class Expression implements Comparable<Expression>, Serializable {

	private static final long serialVersionUID = 1L;

	public abstract void accept(Visitor visitor) throws VisitorException;

	@Override
	public final int compareTo(Expression expression) {
		return toString().compareTo(expression.toString());
	}

	@Override
	public abstract boolean equals(Object object);

	@Override
	public abstract String toString();

}
