package za.ac.sun.cs.green.store;


import java.util.Map;

import za.ac.sun.cs.green.expr.Expression;


public interface ExpressionStore extends Store {

	public Boolean getBoolean(Expression exp);
	
	public Boolean query(Expression exp,Map<String,Object> solution);
	
	public void put(Expression exp,boolean satisfiable,Map<String,Object> solution);
	
	//public void putUnsatCore(Expression exp);

}
