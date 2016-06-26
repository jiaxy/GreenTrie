package cn.edu.whu.sklse.greentrie.z3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.math.fraction.Fraction;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.SATService;
import cn.edu.whu.sklse.SimpleProfiler;
import cn.edu.whu.sklse.greentrie.logic.LogicalRelationUtil;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.RealExpr;
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
		try {
			ctx = new Context(cfg);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error Z3: Exception caught in Z3 JNI: \n" + e);
		}
	}

	@Override
	protected Boolean solve(Instance instance) {
		// Boolean result = false;
		// // translate instance to Z3
		// Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
		// try {
		// instance.getExpression().accept(translator);
		// } catch (VisitorException e1) {
		// log.log(Level.WARNING, "Error in translation to Z3"+e1.getMessage());
		// }
		// // get context out of the translator
		// BoolExpr expr = translator.getTranslation();
		// // model should now be in ctx
		// try {
		// Z3solver = ctx.mkSolver();
		// Z3solver.add(expr);
		// } catch (Z3Exception e1) {
		// log.log(Level.WARNING, "Error in Z3"+e1.getMessage());
		// }
		// //solve
		// try {
		// if (Status.SATISFIABLE == Z3solver.check())
		// result = true;
		// else {
		// result = false;
		// }
		// } catch (Z3Exception e) {
		// log.log(Level.WARNING, "Error in Z3"+e.getMessage());
		// }
		Map<Variable, Object> solution = this.model(instance);
		if (solution != null) {
			instance.setData("solution", solution);
		}

		return solution != null;
	}

	@Override
	protected Operation getUnsatCore(Operation op) {
		List<Operation> expList=new ArrayList<Operation>();
		LogicalRelationUtil.splitIntoList(expList, op);
		if (expList.size() < 3) {
			return op;
		}
		List<Operation> coreList=new ArrayList<Operation>();
		try {
			if (Z3solver == null) {
				Z3solver = ctx.mkSolver();
			} else {
				Z3solver.reset();
			}
			Map<BoolExpr, Operation> assumptionMap = new HashMap<BoolExpr, Operation>();
			BoolExpr[] assumptions = new BoolExpr[expList.size()];
			for (int i = 0; i < expList.size(); i++) {
				Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
				Operation exp = expList.get(i);
				exp.accept(translator);
				BoolExpr z3exp = translator.getTranslation();
				BoolExpr name = ctx.mkBoolConst("P" + i);
				Z3solver.add(ctx.mkOr(z3exp, name));
				assumptions[i] = ctx.mkNot(name);
				assumptionMap.put(assumptions[i], exp);
			}
			Status result = Z3solver.check(assumptions);

			if (result == Status.UNSATISFIABLE) {
				System.out.println("unsat");
				// System.out.println("proof: " + solver.getProof());
				System.out.println("core: ");
				for (Expr c : Z3solver.getUnsatCore()) {
					coreList.add(assumptionMap.get(c));
					System.out.println(assumptionMap.get(c));
				}
				return LogicalRelationUtil.buildOperationFromList(coreList);
			}
		} catch (VisitorException e) {
			e.printStackTrace();
		} catch (Z3Exception e) {
			e.printStackTrace();
		}
		return op;
	}

	protected Map<Variable, Object> model(Instance instance) {
		HashMap<Variable, Object> results = new HashMap<Variable, Object>();
		Map<Variable, Expr> variableMap=new HashMap<Variable, Expr>();
		// translate instance to Z3
		
		List<Operation> opList=new ArrayList<Operation>();
		List<BoolExpr> exprList=new ArrayList<BoolExpr>();
		
		LogicalRelationUtil.splitIntoList(opList, (Operation)instance.getExpression());
		try {
			for(Operation op:opList){
				Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
				op.accept(translator);
				exprList.add(translator.getTranslation());
				variableMap.putAll(translator.getVariableMap());
			}
		} catch (VisitorException e1) {
			e1.printStackTrace();
		}
		
		IncZ3Solver solver=IncZ3Solver.getInstance(ctx);
		// solve
		try { 
			SimpleProfiler.start("solveWithZ3");
			Status r = solver.solve(exprList);
			SimpleProfiler.stop("solveWithZ3");
			if (Status.SATISFIABLE == r) {
				SimpleProfiler.start("getSoluationFromZ3");
				results=solver.getSolution(variableMap);
				SimpleProfiler.stop("getSoluationFromZ3");
			} else {
				log.log(Level.WARNING, "constraint has no model, it is infeasible");
				return null;
			}
		} catch (Z3Exception e) {
			log.log(Level.WARNING, "Error in Z3" + e.getMessage());
		}
		return results;
	}

}
