package cn.edu.whu.sklse.greentrie.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import za.ac.sun.cs.green.expr.Operation;

/**
 * <p>
 * 
 * </p>
 * *
 * 
 * @author Xiangyang Jia
 */
public class TrieNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int depth;
	private final int logicDepth;

	private final TrieNode rootNode;

	private Map<Operation, TrieNode> success = new TreeMap<Operation, TrieNode>();

	private TrieNode previous = null;

	private Operation inAct = null;

	private int maxDepthofSuceess = 0;

	private int minDepthofSuceess = Integer.MAX_VALUE;

	private Map<String, Object> solution = null;

	public TrieNode() {
		this(0, 0);
	}

	public TrieNode(int depth, int logicDepth) {
		this.depth = depth;
		this.logicDepth = logicDepth;
		this.rootNode = depth == 0 ? this : null;
	}

	private TrieNode nextNode(Operation act, boolean ignoreRootState) {
		TrieNode nextState = this.success.get(act);
		if (!ignoreRootState && nextState == null && this.rootNode != null) {
			nextState = this.rootNode;
		}
		return nextState;
	}

	public TrieNode nextNode(Operation act) {
		return nextNode(act, false);
	}

	public TrieNode nextNodeIgnoreRoot(Operation act) {
		return nextNode(act, true);
	}

	public TrieNode addNode(Operation act, List<TrieNode> list, boolean sat,
			int logicDepth) {
		TrieNode nextState = nextNodeIgnoreRoot(act);
		if (nextState == null) {
			nextState = new TrieNode(this.depth + 1, logicDepth);
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

	public Collection<TrieNode> getNodes() {
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

	public Map<Operation, TrieNode> getSuccess() {
		return success;
	}

	public TrieNode getPrevious() {
		return previous;
	}

	public void setPrevious(TrieNode previous) {
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
