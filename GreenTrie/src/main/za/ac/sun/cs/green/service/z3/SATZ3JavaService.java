package za.ac.sun.cs.green.service.z3;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import cn.edu.whu.sklse.SimpleProfiler;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.SATService;

public class SATZ3JavaService extends SATService {
	
	Context ctx;
	Solver Z3solver;
	
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
		Boolean result = false;
		// translate instance to Z3 
		Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
		try {
			instance.getExpression().accept(translator);
		} catch (VisitorException e1) {
			log.log(Level.WARNING, "Error in translation to Z3"+e1.getMessage());
		}
		// get context out of the translator
		SimpleProfiler.start("translateToZ3");
		BoolExpr expr = translator.getTranslation();
		SimpleProfiler.stop("translateToZ3");
		// model should now be in ctx
		SimpleProfiler.start("initZ3");
		try {
			Z3solver = ctx.mkSolver();
			Z3solver.add(expr);
		} catch (Z3Exception e1) {
			log.log(Level.WARNING, "Error in Z3"+e1.getMessage());
		}
		SimpleProfiler.stop("initZ3");
		//solve
		SimpleProfiler.start("solveWithZ3");
		try {
			if (Status.SATISFIABLE == Z3solver.check())
				result = true;
			else {
				result = false;
			}
		} catch (Z3Exception e) {
			log.log(Level.WARNING, "Error in Z3"+e.getMessage());
		}
		SimpleProfiler.stop("solveWithZ3");
		return result;
	}


}
