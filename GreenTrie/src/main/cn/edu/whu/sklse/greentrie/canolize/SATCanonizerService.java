package cn.edu.whu.sklse.greentrie.canolize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import choco.cp.model.managers.operators.SumManager;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.util.Reporter;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.VisitorException;

public class SATCanonizerService extends BasicService {

	/**
	 * Number of times the slicer has been invoked.
	 */
	private int invocations = 0;

	public SATCanonizerService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(getClass());
		if (result == null) {
			final Map<Variable, Variable> map = new HashMap<Variable, Variable>();
			final Expression e = canonize(instance.getFullExpression(), map);
			final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
			result = Collections.singleton(i);
			instance.setData(getClass(), result);
		}
		return result;
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocations = " + invocations);
	}

	public Expression canonize(Expression expression, Map<Variable, Variable> map) {
		try {
			invocations++;
			log.log(Level.FINEST, "start to canonzing expression:"+expression);
			OrderingVisitor orderingVisitor = new OrderingVisitor();
			expression.accept(orderingVisitor);
			Expression orderedExpression = orderingVisitor.getExpression();
			CanonizationVisitor canonizationVisitor = new CanonizationVisitor();
			orderedExpression.accept(canonizationVisitor);
			Expression canonized = canonizationVisitor.getExpression();
			canonized = new Reducer().reduce(canonized);
			if (canonized != null) {
				canonized = new Renamer(map, canonizationVisitor.getVariableSet()).rename(canonized);
			}
			log.log(Level.FINEST, "canonized expression:"+canonized);
			return canonized;
		} catch (VisitorException x) {
			log.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		return null;
	}

}
