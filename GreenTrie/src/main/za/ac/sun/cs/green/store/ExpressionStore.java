package za.ac.sun.cs.green.store;


import java.util.Map;

import za.ac.sun.cs.green.expr.Expression;


public interface ExpressionStore extends Store {

	public Boolean getBoolean(Expression exp);
	
	public Map<String,Object> getSolution(Expression exp);
	
	public void put(Expression exp,boolean satisfiable,Map<String,Object> solution);

}
