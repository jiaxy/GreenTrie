package cn.edu.whu.sklse.greentrie.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cn.edu.whu.sklse.greentrie.logic.LogicalRelationUtil;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;

/**
 * 
 * @author Jiaxiangyang
 */
@SuppressWarnings("rawtypes")
public class Trie implements Serializable {

	private static final long serialVersionUID = 1L;

	TrieNode rootState;

	private Properties config;

	private int patternCount = 0;

	private Map<String, List<Operation>> implicationTreeHeads = new HashMap<String, List<Operation>>();

	private Map<String, Expression> expMap = new HashMap<String, Expression>();

	private Map<Operation, List<TrieNode>> expPositions = new HashMap<Operation, List<TrieNode>>(); 
	


	public Trie() {
		this.rootState = new TrieNode();
	}

	public Trie(Properties config) {
		this.config = config;
	}

	public void saveConstraint(List<Operation> constraintList, Map<String,Object> solution,	boolean sat) {
		int size = constraintList.size();
		if (constraintList == null || size == 0) {
			return;
		}
		TrieNode currentState = this.rootState;
		Expression prePrefix=null;
		int logicDepth=0;
		for (int i = 0; i < size; i++) {
			// System.out.print("\n path: ");
			currentState.setMaxDepthofSuceess(Math.max(
					currentState.getMaxDepthofSuceess(), size - i));
			currentState.setMinDepthofSuceess(Math.min(
					currentState.getMinDepthofSuceess(), size - i));
			Operation exp = (Operation) this.getAndPutExpression(constraintList.get(i)
					.toString(), constraintList.get(i));
			Expression prefix = LogicalRelationUtil.getPrefix(exp);
			if(!prefix.equals(prePrefix)){
				logicDepth++;
				prePrefix=prefix;
			}
			String prefixStr = prefix.toString();
			Expression p = getAndPutExpression(prefixStr, prefix);
			LogicalRelationUtil.setPrefix(exp, p);
			List<Operation> heads = getImplicationTree(prefixStr);
			exp = LogicalRelationUtil.insertIntoImplyGraph(heads, exp);
			currentState = currentState.addNode(exp, getExpPositions(exp),sat,logicDepth);
		}
		currentState.setSolution(solution);
		if(!sat&&!currentState.getSuccess().isEmpty()){
			//found a longer constraints in UNSAT store
			currentState.getSuccess().clear();
		}
		patternCount++;
	}

	private List<TrieNode> getExpPositions(Operation exp) {
		List<TrieNode> heads = this.expPositions.get(exp);
		if (heads == null) {
			heads = new ArrayList<TrieNode>();
			this.expPositions.put(exp, heads);
		}
		return heads;
	}

	private List<Operation> getImplicationTree(String key) {
		List<Operation> heads = this.implicationTreeHeads.get(key);
		if (heads == null) {
			heads = new ArrayList<Operation>();
			this.implicationTreeHeads.put(key, heads);
		}
		return heads;
	}

	private Expression getAndPutExpression(String key, Expression exp) {
		Expression e = this.expMap.get(key);
		if (e == null) {
			e = exp;
			this.expMap.put(key, e);
		}
		return e;
	}

//	@SuppressWarnings("unchecked")
//	public boolean hasSubset2(List<Operation> target) {
//		List<Operation> target2 = new ArrayList<Operation>();
//		List<List<Operation>> implyList =new ArrayList<List<Operation>>();
//		for(Operation o:target){
//			Expression prefix = LogicalRelationUtil.getCononizedPrefix(o);
//			Expression prefix2 = this.getExpMap().get(prefix.toString());
//			if (prefix2 == null) {
//				continue;
//			}
//			LogicalRelationUtil.setCononizedPrefix(o, prefix2);
//			List<Operation> imply = LogicalRelationUtil.getImply(getImplicationTree(prefix.toString()), o);
//			if(imply.isEmpty()){
//				continue;
//			}
//			implyList.add(imply);
//			target2.add(o);
//		}
//		if(target2.size()==0){
//			return false;
//		}
//		return hasSubset(this.rootState, target2, 0,implyList);
//	}
	
	@SuppressWarnings("unchecked")
	public boolean hasSubset(List<Operation> target) {
		long t0=System.currentTimeMillis();
		Set<Operation> implySet = getImplySet(target);
		if(implySet.isEmpty()){
			return false;
		}
//		List<Operation> lastopList = implyList.get(size - 1);
//		for (Operation o : lastopList) {
//			List<State> states = this.expPositions.get(o);
//			if(states!=null){
//				for (State s : states) {
//					if(!s.emit().isEmpty()&&s.getDepth()<=size){
//						boolean r = hasSubset2(s.getPrevious(),implySet);
//						if(r){
//							return true;
//						}
//					}
//				}
//			}
//		}
		//return false;
	boolean result = hasSubset(this.rootState, implySet);
	//report.put("travel_Time", (System.currentTimeMillis()-t0));
	return result;
	}

	private Set<Operation> getImplySet(List<Operation> target) {
		//List<Operation> target2 = new ArrayList<Operation>();
		List<List<Operation>> implyList =new ArrayList<List<Operation>>();
		Set<Operation> implySet=new HashSet<Operation>();
		for(Operation o:target){
			Expression prefix = LogicalRelationUtil.getPrefix(o);
			Expression prefix2 = this.getExpMap().get(prefix.toString());
			if (prefix2 == null) {
				continue;
			}
			LogicalRelationUtil.setPrefix(o, prefix2);
			List<Operation> imply = LogicalRelationUtil.getImply(getImplicationTree(prefix.toString()), o);
			if(imply.isEmpty()){
				continue;
			}
			implyList.add(imply);
			implySet.addAll(imply);
			//target2.add(o);
		}
		//long t1=System.currentTimeMillis();
		//report.put("implySet_buildTime", (t1-t0));
//		int size=target2.size();
//		if(target2.size()==0){
//			return false;
//		}
		return implySet;
	}
	
	private boolean hasSubset(TrieNode s, Set<Operation> implySet) {
		if(s.getSuccess().isEmpty()){
			return true;
		}
		for(Operation o:s.getSuccess().keySet()){
			if(implySet.contains(o)){
				boolean r = hasSubset(s.nextNode(o),implySet);
				if(r){
					return true;
				}
			}
		}
		return false;
	}
	
	public  void getCandidateSuluation(TrieNode s, List<Operation> expList,List<Map<String, Object>> solutions) {
		Set<Operation> implySet = getImplySet(expList);
		if(implySet.isEmpty()){
			return;
		}
		
		getCandidateSuluation(s,implySet,solutions) ;
		
		
	}
	
	private  void getCandidateSuluation(TrieNode s, Set<Operation> implySet,List<Map<String, Object>> solutions) {
		if(s.getSuccess().isEmpty()&&s!=this.rootState){
			solutions.add(s.getSolution());
			return;
		}
		for(Operation o:s.getSuccess().keySet()){
			if(implySet.contains(o)){
				getCandidateSuluation(s.nextNode(o),implySet,solutions);
			}
		}
	}
	

	@SuppressWarnings("unchecked")
	private boolean hasSubset(TrieNode currentState, List<Operation> target, int i, List<List<Operation>> implyList) {
		int size = target.size();
//		if (currentState.getMinDepthofSuceess() > size - i) {
//			return false;
//		}
		for (int j = i; j < size; j++) {
			// System.out.println("i="+i+",j="+j);
			// System.out.println("find "+target.get(j)+" in "+currentState.getSuccess().keySet());
			List<Operation> list = implyList.get(j);
			for (Operation o : list) {
				TrieNode next = currentState.getSuccess().get(o);
				if (next != null) {
					if (next.getSuccess().isEmpty()) {
						return true;
					} else if (hasSubset(next, target, j + 1,implyList)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean hasSuperSet(List<Operation> target) {
		List<List<Operation>> impliedList =new ArrayList<List<Operation>>();
		List<Integer> logicDepthList=new ArrayList<Integer>();
		//Set<Operation> impliedSet=new HashSet<Operation>();
		Expression prePrefix=null;
		int logicDepth=0;
		for (Operation o : target) {
			Expression prefix = LogicalRelationUtil.getPrefix(o);
			if(!prefix.equals(prePrefix)){
				logicDepth++;
				prePrefix=prefix;
			}
			logicDepthList.add(logicDepth);
			Expression prefix2 = this.getExpMap().get(prefix.toString());
			if (prefix2 == null) {
				return false;
			}
			LogicalRelationUtil.setPrefix(o, prefix2);
			List<Operation> implied = LogicalRelationUtil.getBeImplied(
					getImplicationTree(prefix.toString()), o);
			if (implied.isEmpty()) {
				return false;
			}
			if (implied.contains(o)) {//set itself as the first element
				implied.remove(o);
				implied.add(0,o);
			}
			impliedList.add(implied);
		}
		int size = target.size();
		List<Operation> lastopList = impliedList.get(size - 1);
		int pathCount=0;
		for (Operation o : lastopList) {
			List<TrieNode> states = this.expPositions.get(o);
			if(states==null){
				return false;
			}
			for (TrieNode s : states) {
//				
//				if(s.getLogicDepth()<logicDepthList.get(size)-1){
//					continue;
//				}
//				
				if (isSuperSet(s.getPrevious(), size - 2,impliedList,logicDepthList)) {
					long t2=System.currentTimeMillis();
					return true;
				}
			}
		}
		return false;
	}

	private boolean isSuperSet(TrieNode s, int end, List<List<Operation>> impliedList, List<Integer> logicDepthList) {
		while (s != null&&end >= 0&&s.getLogicDepth()>=logicDepthList.get(end)) {  //&& s.getDepth() > end
			while(end >= 0 &&impliedList.get(end).contains(s.getInAct())){// sometimes one constraint in superset implies multiple constraints in subset
				end--;
			}
			s = s.getPrevious();
		}
		if (end < 0) {
			return true;
		} else {
			return false;
		}
	}

	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// private boolean hasSuperSet(State currentState,List<Operation> target,
	// int pos) {
	// if (pos >= target.size()) {
	// return false;
	// }
	// if (currentState.getMaxDepthofSuceess() < target.size() - pos) {
	// return false;
	// }
	//
	//
	// for(int j=pos;j<target.size();j++){
	// Operation op = target.get(j);
	//
	// }
	//
	//
	//
	//
	// }

//	public void printTrie(State state, List<Operation> path) {
//		if (!state.emit().isEmpty()) {
//			System.out.println("constraint:" + state.emit());
//			System.out.println("path:" + path);
//		}
//		for (Operation edge : state.getTransitions()) {
//			State next = state.nextState(edge);
//			List<Operation> l = new ArrayList<Operation>();
//			l.addAll(path);
//			printTrie(next, l);
//		}
//	}

	public TrieNode getRootState() {
		return rootState;
	}

	public int getPatternCount() {
		return patternCount;
	}

	public Map<String, List<Operation>> getImplicationTreeHeads() {
		return implicationTreeHeads;
	}

	public Map<String, Expression> getExpMap() {
		return expMap;
	}

	public void setExpMap(Map<String, Expression> expMap) {
		this.expMap = expMap;
	}
	
	public Map<Operation, List<TrieNode>> getExpPositions() {
		return expPositions;
	}

	// private int logicalCompare(E e,E head) {
	// int r = e.compareTo(head);
	// for(LogicalMatcher matcher:matcherList){
	// if(matcher.isMatched((Operation)e, (Operation)head)){
	// r=0;
	// break;
	// }
	// }
	// return r;
	// }

	// private State<E> getState(State<E> currentState, E[] target, int i) {
	// State<E> newCurrentState = currentState.nextState(target[i]);
	// // TODO
	// while (newCurrentState == null) {
	// currentState = currentState.failure();
	// if (currentState == null) {
	// newCurrentState = this.rootState;
	// } else {
	// newCurrentState = currentState.nextState(target[i]);
	// }
	// }
	// return newCurrentState;
	// }

	// private void checkForConstructedFailureStates() {
	// if (!this.failureStatesConstructed) {
	// constructFailureStates();
	// }
	// }

	// public void constructFailureStates() {
	// Queue<State<E>> queue = new LinkedBlockingDeque<State<E>>();
	//
	// // First, set the fail state of all depth 1 states to the root state
	// for (State<E> depthOneState : this.rootState.getStates()) {
	// depthOneState.setFailure(this.rootState);
	// queue.add(depthOneState);
	// }
	// this.failureStatesConstructed = true;
	//
	// // Second, determine the fail state for all depth > 1 state
	// while (!queue.isEmpty()) {
	// State<E> currentState = queue.remove();
	//
	// for (E transition : currentState.getTransitions()) {
	// State<E> targetState = currentState.nextState(transition);
	// queue.add(targetState);
	//
	// State<E> traceFailureState = currentState.failure();
	// while (traceFailureState.nextState(transition) == null) {
	// traceFailureState = traceFailureState.failure();
	// }
	// State<E> newFailureState = traceFailureState.nextState(transition);
	// targetState.setFailure(newFailureState);
	// targetState.addEmit(newFailureState.emit());
	// }
	// }
	// }

	// private void storeEmits(int position, State<E> currentState, List<Emit>
	// collectedEmits) {
	// Collection<String> emits = currentState.emit();
	// if (emits != null && !emits.isEmpty()) {
	// for (String emit : emits) {
	// collectedEmits.add(new Emit(position - emit.length() + 1, position,
	// emit));
	// }
	// }
	// }

}
