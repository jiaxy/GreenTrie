package cn.edu.whu.sklse.greentrie.coral;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.Properties;

import symlib.SymBool;
import symlib.SymLiteral;
import symlib.SymNumber;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.SATService;
import coral.PC;
import coral.solvers.Env;
import coral.solvers.Result;
import coral.solvers.Solver;
import coral.solvers.SolverKind;
import coral.util.Config;

public class SATCoralService extends SATService {
	private SolverKind solverKind;
	private coral.PC pc = new coral.PC();
	Env sol = null;
	
	public SATCoralService(Green s,Properties properties) {
		super(s);
		configure(properties);
	}
	


	public coral.PC getPc() {
		return pc;
	}
	
	@Override
	protected Boolean solve(Instance instance) {
		Expression exp = instance.getFullExpression();
		CoralTranslator translator=new CoralTranslator();
		try {
			exp.accept(translator);
			SymBool symbool = translator.getTranslation();
			ArrayList<SymBool> constraints = new ArrayList<SymBool>();
			constraints.add(symbool);
			pc.setConstraints(constraints);
			//pc.addConstraint(translator.getTranslation());
			Solver solver = solverKind.get();
			Boolean result = false;
			sol = solveIt(pc, solver);
			if (sol.getResult() == Result.SAT) {
				result = true;
			}
			
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	}
	
	@SuppressWarnings("unused")
	private Env solveIt(final PC pc, final Solver solver) throws InterruptedException {
		final Env[] env = new Env[1];
		Runnable solverJob = new Runnable() {
			@Override
			public void run() {
				try {
					env[0] = solver.getCallable(pc).call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		/**
		 * If solving is based on timeouts (value > 0)
		 * the code spawns a timer thread.  otherwise,
		 * it calls the run() method directly.
		 */
//		if (timeout > 0) { // old code; not executed
//			Thread t = new Thread(solverJob);
//			t.start();
//			t.join(timeout);
//			solver.setPleaseStop(true);
//			Thread.sleep(10);
//		} else {
		solverJob.run();
//		}
		return env[0];
	}

	public double getRealValueInf(Object dpvar) {
		return -1;
	}

	public double getRealValueSup(Object dpVar) {
		return -1;
	}

	public double getRealValue(Object dpVar) {
		SymNumber symNumber = sol.getValue((SymLiteral)dpVar);
		return symNumber.evalNumber().doubleValue();
	}

	public int getIntValue(Object dpVar) {
		SymNumber symNumber = sol.getValue((SymLiteral)dpVar);
		try {
		return symNumber.evalNumber().intValue();
		} catch (NullPointerException e) {
			throw e;
		}
	}
	
	
	public void configure(Properties properties) {
		gov.nasa.jpf.Config conf=null;
		if(VM.getVM()!=null){
			conf = VM.getVM().getConfig();
		}else{
			conf=JPF.createConfig(new String[]{});
			conf.putAll(properties);
		}
		
		long seed = conf.getLong("coral.seed",464655);
		int nIterations = conf.getInt("coral.iterations",-1);
		SolverKind kind = SolverKind.valueOf(conf.getString("coral.solver","PSO_OPT4J").toUpperCase());
		boolean optimize = conf.getBoolean("coral.optimize", true);
		String intervalSolver = conf.getString("coral.interval_solver","none").toLowerCase();
		String intervalSolverPath = conf.getString("coral.interval_solver.path","none");
		
		Config.seed = seed;
		solverKind = kind;
		if(optimize) {
			Config.toggleValueInference = true;
			Config.removeSimpleEqualities = true;
		}
		
		if(!intervalSolver.equals("none")) {
			Config.intervalSolver = intervalSolver;
			Config.enableIntervalBasedSolver = true;
			if(intervalSolver.equals("realpaver")) {
				Config.realPaverLocation = intervalSolverPath;
			} else if (intervalSolver.equals("icos")) {
				Config.icosLocation = intervalSolverPath;
			} else {
				throw new RuntimeException("Unsupported interval solver!");
			}
			
			Config.simplifyUsingIntervalSolver = optimize ? true : false;
		}
		
		/**
		 * setting maximum number of iterations allowed.
		 * the solver return with no solution in that
		 * case.  note that the constraint may still be
		 * satisfiable.
		 */
		if(nIterations != -1) {
			if(kind.equals(SolverKind.PSO_OPT4J)) {
				Config.nIterationsPSO = nIterations;
			} else if(kind.equals(SolverKind.RANDOM)) {
				Config.nIterationsRANDOM = nIterations;
			} else if(kind.equals(SolverKind.AVM)) {
				Config.nIterationsAVM = nIterations;
			} 
		}
	}
	

}
