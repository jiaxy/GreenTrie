package gov.nasa.jpf.symbc.green.trie;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import za.ac.sun.cs.green.expr.Operation;

/**
 * <p>
 * 
 * </p>
 * *
 * 
 * @author Jia Xiangyang
 */
public class State implements Serializable {
	private static final long serialVersionUID = 1L;
	/** effective the size of the keyword */
	private final int depth;
	private final int logicDepth;

	/**
	 * only used for the root state to refer to itself in case no matches have
	 * been found
	 */
	private final State rootState;

	/**
	 * referred to in the white paper as the 'goto' structure. From a state it
	 * is possible to go to other states, depending on the character passed.
	 */
	private Map<Operation, State> success = new TreeMap<Operation, State>();

	private State previous = null;

	private Operation inAct = null;

	private int maxDepthofSuceess = 0;

	private int minDepthofSuceess = Integer.MAX_VALUE;

	/**
	 * whenever this state is reached, it will emit the matches keywords for
	 * future reference
	 */
	private Map<String, Object> solution = null;

	public State() {
		this(0, 0);
	}

	public State(int depth, int logicDepth) {
		this.depth = depth;
		this.logicDepth = logicDepth;
		this.rootState = depth == 0 ? this : null;
	}

	private State nextState(Operation act, boolean ignoreRootState) {
		State nextState = this.success.get(act);
		if (!ignoreRootState && nextState == null && this.rootState != null) {
			nextState = this.rootState;
		}
		return nextState;
	}

	public State nextState(Operation act) {
		return nextState(act, false);
	}

	public State nextStateIgnoreRootState(Operation act) {
		return nextState(act, true);
	}

	public State addState(Operation act, List<State> list, boolean sat,
			int logicDepth) {
		State nextState = nextStateIgnoreRootState(act);
		if (nextState == null) {
			nextState = new State(this.depth + 1, logicDepth);
			nextState.previous = this;
			nextState.inAct = act;
			this.success.put(act, nextState);
			list.add(nextState);
		}
		return nextState;
	}

	public int getDepth() {
		return this.depth;
	}

	public Map<String, Object> getSolution() {
		return solution;
	}

	public void setSolution(Map<String, Object> solution) {
		this.solution = solution;
	}

	public Collection<State> getStates() {
		return this.success.values();
	}

	public SortedSet<Operation> getTransitions() {
		return (SortedSet<Operation>) this.success.keySet();
	}

	public int getMaxDepthofSuceess() {
		return maxDepthofSuceess;
	}

	public void setMaxDepthofSuceess(int maxDepthofSuceess) {
		this.maxDepthofSuceess = maxDepthofSuceess;
	}

	public int getMinDepthofSuceess() {
		return minDepthofSuceess;
	}

	public void setMinDepthofSuceess(int minDepthofSuceess) {
		this.minDepthofSuceess = minDepthofSuceess;
	}

	public Map<Operation, State> getSuccess() {
		return success;
	}

	public State getPrevious() {
		return previous;
	}

	public void setPrevious(State previous) {
		this.previous = previous;
	}

	public Operation getInAct() {
		return inAct;
	}

	public void setInAct(Operation inAct) {
		this.inAct = inAct;
	}

	public int getLogicDepth() {
		return logicDepth;
	}

}
