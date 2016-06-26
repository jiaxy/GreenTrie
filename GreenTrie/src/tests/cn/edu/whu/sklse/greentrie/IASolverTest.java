 package cn.edu.whu.sklse.greentrie;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.util.Configuration;


public class IASolverTest {
	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		
		props.setProperty("green.taskmanager", "za.ac.sun.cs.green.taskmanager.ParallelTaskManager");
		props.setProperty("green.store", "cn.edu.whu.sklse.greentrie.store.TrieStore");
		
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "iasolver");
		props.setProperty("green.service.sat.slice",
				"cn.edu.whu.sklse.greentrie.slice.SATSlicerService");
		props.setProperty("green.service.sat.canonize",
				"cn.edu.whu.sklse.greentrie.canolize.SATCanonizerService");
		props.setProperty("green.service.sat.iasolver",
				"cn.edu.whu.sklse.greentrie.iasolver.SATIASolverService");
		Configuration config = new Configuration(solver, props);
		config.configure();
		
	}



	@AfterClass
	public static void report() {
		if (solver != null) {
			solver.report();
		}
	}
	
	@Test
	public void test() {
		RealVariable v = new RealVariable("v0", 0.0, 99.0);
		RealConstant c = new RealConstant(0.0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		check(o,null,true);
		check(o,null,true);
	}
	
	@Test
	public void test03() {
		RealVariable v = new RealVariable("aa", 0.0, 99.9);
		RealConstant c1 = new RealConstant(10);
		RealConstant c2 = new RealConstant(20);
		Operation o1 = new Operation(Operation.Operator.EQ, v, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		check(o3,null,false);
	}
	
	
	@Test
	public void test02() {
		RealConstant c1 = new RealConstant(2);
		RealVariable v1 = new RealVariable("aa",  0.0, 99.9);
		RealVariable v2 = new RealVariable("bb",  0.0, 99.9);
		Operation o1 = new Operation(Operation.Operator.SUB, v1, v2);
		Operation o2 = new Operation(Operation.Operator.MUL, c1, o1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, v1);
		check(o3, null, true);
	} 
	
	@Test
	public void test04() {
		RealConstant c1 = new RealConstant(2);
		RealVariable v1 = new RealVariable("aa",  0.0, 99.9);
		RealVariable v2 = new RealVariable("bb",  0.0, 99.9);
		Operation o1 = new Operation(Operation.Operator.SUB, v1, v2);
		Operation o2 = new Operation(Operation.Operator.ABS, o1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, c1);
		check(o3, null, true);
	}
	
	@Test
	public void test05() {
		RealConstant c1 = new RealConstant(-2);
		RealVariable v1 = new RealVariable("aa",  0.0, 99.9);
		Operation o1 = new Operation(Operation.Operator.LT, v1, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v1,c1);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		check(o3, null, false);
	}
	
	@Test
	public void test06() {
		RealConstant c1 = new RealConstant(-2);
		RealVariable v1 = new RealVariable("aa",  0.0, 99.9);
		RealVariable v2 = new RealVariable("bb",  0.0, 99.9);
		Operation o1 = new Operation(Operation.Operator.SUB, v1, v2);
		Operation o2 = new Operation(Operation.Operator.SQRT, o1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, c1);
		check(o3, null, false);
	}

	private void check(Expression expression, Expression parentExpression,
			boolean expected) {
		Instance p = (parentExpression == null) ? null : new Instance(solver,
				null, parentExpression);
		Instance i = new Instance(solver, p, expression);
		Object result = i.request("sat");
		assertNotNull(result);
		assertEquals(Boolean.class, result.getClass());
		assertEquals(expected, result);
	}
	
	
}
