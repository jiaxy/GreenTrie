package cn.edu.whu.sklse.greentrie.iasolver;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.VM;
import ia_parser.Exp;
import ia_parser.IAParser;
import ia_parser.RealIntervalTable;

import java.util.ArrayList;
import java.util.Properties;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.SATService;

public class SATIASolverService extends SATService {
	RealIntervalTable vars;
	
	public SATIASolverService(Green s,Properties properties) {
		super(s);
	}
	
	boolean narrow(Exp exp, int numNarrows) {
		vars = new RealIntervalTable();
		exp.bindVars(vars);
		for (int i = 0; i <= numNarrows; i++) {
			if (!exp.narrow()) {
				//System.out.println("narrow failed");
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	protected Boolean solve(Instance instance) {
		//String c = pb;
		Expression expr = instance.getFullExpression();
		IASolverTranslator translator=new IASolverTranslator();
		try {
			expr.accept(translator);
			String c = translator.getTranslation();
			if(c==null || "".equals(c))
				return true;
			Exp exp = IAParser.parseString(c);
			int max_narrow = 100;
			return narrow(exp, max_narrow);
		} catch (Exception e) {
			//throw new RuntimeException(e);
			throw new RuntimeException("## Error IASolver: "+e);
		} 
	}
	
}
