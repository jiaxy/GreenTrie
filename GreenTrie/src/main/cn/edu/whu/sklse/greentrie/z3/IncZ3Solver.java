package cn.edu.whu.sklse.greentrie.z3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import org.apache.commons.math.fraction.Fraction;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import za.ac.sun.cs.green.expr.Variable;


public class IncZ3Solver{

	private static IncZ3Solver instance;
	
	private Solver solver=null;
	
	Stack<BoolExpr> pre=new Stack<BoolExpr>();

	private IncZ3Solver(Context ctx) {
		super();
		try {
			solver=ctx.mkSolver();
		} catch (Z3Exception e) {
			e.printStackTrace();
		}
	}
	
	public static IncZ3Solver getInstance(Context ctx){
		if(instance==null){
			instance=new IncZ3Solver(ctx);
		}
		return instance;
	}
	
	public Status solve(List<BoolExpr> cur) throws Z3Exception{
		int base=0;
		int minsize=Math.min(pre.size(), cur.size());
		while(base<minsize){
			if(!pre.get(base).equals(cur.get(base))){
				break;
			}
			base++;
		}
		if(base<pre.size()){
			int count=pre.size()-base;
			solver.pop(count);
			for(int i=0;i<count;i++){
				pre.pop();
			}
		}
		if(base<cur.size()){
			int count=cur.size()-base;
			for(int i=0;i<count;i++){
				BoolExpr item = cur.get(base+i);
				pre.push(item);
				solver.push();
				solver.add(item);
			}
		}
		return solver.check();
	}
	
	public HashMap<Variable, Object> getSolution(Map<Variable, Expr> variableMap) throws Z3Exception{
		HashMap<Variable, Object> results = new HashMap<Variable, Object>();
		Model model = solver.getModel();
		for (Map.Entry<Variable, Expr> entry : variableMap.entrySet()) {
			Variable greenVar = entry.getKey();
			Expr z3Var = entry.getValue();
			Expr z3Val = model.evaluate(z3Var, false);
			Object val = null;
			if (z3Val.isIntNum()) {
				val = Integer.parseInt(z3Val.toString());
			} else if (z3Val.isRatNum()) {
				RatNum rv = (RatNum) z3Val;
				val = new Fraction(rv.getNumerator().getInt(), rv.getDenominator().getInt());
			} else {
				throw new Z3Exception("Error unsupported type for variable " + z3Val);
			}
			results.put(greenVar, val);
		}
		return results;
	}
	
}
