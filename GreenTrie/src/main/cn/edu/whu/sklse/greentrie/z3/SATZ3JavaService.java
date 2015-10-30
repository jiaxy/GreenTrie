package cn.edu.whu.sklse.greentrie.z3;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.SATService;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class SATZ3JavaService extends SATService {
	
	Context ctx;
	private static Solver Z3solver;
	
	public SATZ3JavaService(Green solver, Properties properties) {
		super(solver);
		HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
		try{
			ctx = new Context(cfg);		
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error Z3: Exception caught in Z3 JNI: \n" + e);
	    }
	}

	@Override
	protected Boolean solve(Instance instance) {
//		Boolean result = false;
//		// translate instance to Z3 
//		Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
//		try {
//			instance.getExpression().accept(translator);
//		} catch (VisitorException e1) {
//			log.log(Level.WARNING, "Error in translation to Z3"+e1.getMessage());
//		}
//		// get context out of the translator
//		BoolExpr expr = translator.getTranslation();
//		// model should now be in ctx
//		try {
//			Z3solver = ctx.mkSolver();
//			Z3solver.add(expr);
//		} catch (Z3Exception e1) {
//			log.log(Level.WARNING, "Error in Z3"+e1.getMessage());
//		}
//		//solve 		
//		try {
//			if (Status.SATISFIABLE == Z3solver.check())
//				result = true;
//			else {
//				result = false;
//			}
//		} catch (Z3Exception e) {
//			log.log(Level.WARNING, "Error in Z3"+e.getMessage());
//		}
		Map<Variable, Object> solution=this.model(instance);
		if(solution!=null){
			instance.getRoot().setData("solution", solution);
		}
		
		
		return solution!=null;
	}
	

	protected Map<Variable, Object> model(Instance instance) {		
		HashMap<Variable,Object> results = new HashMap<Variable, Object>();
		// translate instance to Z3 
		Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
		try {
			instance.getExpression().accept(translator);
		} catch (VisitorException e1) {
			log.log(Level.WARNING, "Error in translation to Z3"+e1.getMessage());
		}
		// get context out of the translator
		BoolExpr expr = translator.getTranslation();
		// model should now be in ctx
		try {
			if (Z3solver==null){
				Z3solver=ctx.mkSolver();
			}else{
				Z3solver.reset();
			}
			Z3solver.add(expr);
		} catch (Z3Exception e1) {
			log.log(Level.WARNING, "Error in Z3"+e1.getMessage());
		}
		//solve 		
		try { // Real Stuff is still untested
			long t1 = System.currentTimeMillis();
			if (Status.SATISFIABLE == Z3solver.check()) {
				long t2=System.currentTimeMillis();
				System.out.println("time for check SAT:"+(t2-t1));
				Map<Variable, Expr> variableMap = translator.getVariableMap();
				Model model = Z3solver.getModel();
				for(Map.Entry<Variable,Expr> entry : variableMap.entrySet()) {
					Variable greenVar = entry.getKey();
					Expr z3Var = entry.getValue();
					Expr z3Val = model.evaluate(z3Var, false);
					Object val = null;
					if (z3Val.isIntNum()) {
						val = Integer.parseInt(z3Val.toString());
					} else if (z3Val.isRatNum()) {
						val = Double.parseDouble(z3Val.toString());
					} else {
						log.log(Level.WARNING, "Error unsupported type for variable " + z3Val);
						return null;
					}
					results.put(greenVar, val);
					String logMessage = "" + greenVar + " has value " + val;
					log.log(Level.INFO,logMessage);
				}
				long t3=System.currentTimeMillis();
				System.out.println("time for mapping model:"+(t3-t2));
				
			} else {
				log.log(Level.WARNING,"constraint has no model, it is infeasible");
				Expr[] exps=Z3solver.getUnsatCore();
				System.out.println("unsat core:"+exps);
				
				return null;
			}
		} catch (Z3Exception e) {
			log.log(Level.WARNING, "Error in Z3"+e.getMessage());
		}
		return results;
	}


}