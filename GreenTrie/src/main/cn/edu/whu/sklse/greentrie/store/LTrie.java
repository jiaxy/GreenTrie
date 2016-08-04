package cn.edu.whu.sklse.greentrie.store;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.lambdazen.bitsy.BitsyGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import cn.edu.whu.sklse.SimpleProfiler;
import cn.edu.whu.sklse.greentrie.logic.LogicalRelationUtil;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;

public class LTrie {

	static final String PROP_NAME = "name";
	static final String PROP_EXP = "exp";
	static final String PROP_SOLUTION = "solution";

	static final String EDGE_HAS_PREFIX = "hasPrefix";
	static final String EDGE_IMPLY = "imply";
	static final String EDGE_HAS_REF = "hasRef";
	static final String EDGE_NEXT = "next";

	static final String TREE_ROOT = "tree_root";
	static final String INDEX_ROOT = "index_root";

	static final String KEY_EXP = "exp:";
	static final String KEY_REF = "ref:";
	static final String KEY_PREF = "pref:";

	BitsyGraph graph = null;
	boolean persistent = false;
	Vertex treeRoot=null;
	Vertex indexRoot=null;

	public LTrie() {
		graph = new BitsyGraph();
		graph.createKeyIndex(PROP_NAME, Vertex.class);
		treeRoot = graph.addVertex(null);
		treeRoot.setProperty(PROP_NAME, TREE_ROOT);
		indexRoot = graph.addVertex(null);
		indexRoot.setProperty(PROP_NAME, INDEX_ROOT);
		graph.commit();
	}

	public LTrie(String path) {
		persistent = true;
		// load graph
		File d = new File(path);
		if (!d.exists()) {
			d.mkdirs();
		}
		Path dbPath = Paths.get(path);
		graph = new BitsyGraph(dbPath);
		// create index
		Set<String> keys = graph.getIndexedKeys(Vertex.class);
		if (keys.isEmpty()) {// Empty store
			graph.createKeyIndex(PROP_NAME, Vertex.class);
		}
		// initialize tree_root
		Iterable<Vertex> r1 = graph.getVertices(PROP_NAME, TREE_ROOT);
		if (r1.iterator().hasNext()) {
			treeRoot=r1.iterator().next();
		} else {
			treeRoot = graph.addVertex(null);
			treeRoot.setProperty(PROP_NAME, TREE_ROOT);
		}
		
		Iterable<Vertex> r2 = graph.getVertices(PROP_NAME, INDEX_ROOT);
		if (r2.iterator().hasNext()) {
			indexRoot=r2.iterator().next();
		} else {
			indexRoot = graph.addVertex(null);
			indexRoot.setProperty(PROP_NAME, INDEX_ROOT);
		}
	}

	public LTrie(Properties config) {
		// this.config = config;
	}

	public void shutdown() {
		if (this.persistent) {
			graph.flushTxLog();
		}
		graph.shutdown();
	}

	public void saveConstraint(List<Operation> constraintList, Map<String, Object> solution, boolean sat) {
		assert (constraintList != null && constraintList.size() > 0);
		Object treerootId=treeRoot.getId();
		Object indexrootId=indexRoot.getId();
		//Iterable<Vertex> r1 = graph.getVertices(PROP_NAME, TREE_ROOT);
		Vertex cur =treeRoot;
		for (Operation op : constraintList) {
			Vertex expV = updateIndexGraph(op);
			cur = addTreeNode(cur, op, expV);
			
		}
		if(sat){
			cur.setProperty(PROP_SOLUTION, solution);
		}
		graph.commit();
//		treeRoot=graph.getVertex(treerootId);
//		indexRoot=graph.getVertex(indexrootId);
		//printGraph();
	}

	public Vertex querySuperset(List<Operation> constraintList) {
		List<List<Vertex>> RISList = new ArrayList<List<Vertex>>();
		for (Operation exp : constraintList) {
			List<Vertex> ris = this.getRIS(exp);
			if (ris.isEmpty()) {
				return null;
			}
			RISList.add(this.getRIS(exp));
		}
		SimpleProfiler.start("isSuperSet");
		int size = constraintList.size();
		List<Vertex> lastopList = RISList.get(size - 1);
		for (Vertex ev : lastopList) {
			Iterable<Edge> edges = ev.getEdges(Direction.OUT, EDGE_HAS_REF);
			for (Edge e : edges) {
				Vertex rv = e.getVertex(Direction.IN);
				if (isSuperSet(rv, size - 2, RISList)) {
					SimpleProfiler.stop("isSuperSet");
					return rv;
				}
			}
		}
		SimpleProfiler.stop("isSuperSet");
		return null;
	}

	private boolean isSuperSet(Vertex rv, int end, List<List<Vertex>> RISList) {
		//System.out.println("isSuperSet: "+rv.getProperty("name"));
		while (end >= 0&&(rv!=treeRoot)) {
			SimpleProfiler.start("isSuperSet-1");
			Vertex er =this.getExpVertex(rv);
			SimpleProfiler.stop("isSuperSet-1");
			
			while (end >= 0 && RISList.get(end).contains(er)) {
				end--;
			}
			SimpleProfiler.start("isSuperSet-2");
			Iterable<Edge> edge2 = rv.getEdges(Direction.IN, EDGE_NEXT);
			rv = (edge2.iterator().hasNext()) ? edge2.iterator().next().getVertex(Direction.OUT) : null;
			SimpleProfiler.stop("isSuperSet-2");
		}
		if (end < 0) {
			return true;
		} else {
			return false;
		}
	}

	public Vertex querySubset(List<Operation> constraintList) {
		Set<Vertex> IS = new HashSet<Vertex>();
		for (Operation exp : constraintList) {
			IS.addAll(this.getIS(exp));
		}
		if (IS.isEmpty()) {
			return null;
		}
		//Vertex root = this.treeRoot;
		SimpleProfiler.start("querySubsetInTree");
		Vertex r = querySubsetInTree( this.treeRoot, IS);
		SimpleProfiler.stop("querySubsetInTree");
		 return r;
	}

	private Vertex querySubsetInTree(Vertex root, Set<Vertex> IS) {
	
		//System.out.println("querySubsetInTree: "+root.getProperty("name"));
		Iterable<Edge> edges = root.getEdges(Direction.OUT, EDGE_NEXT);
		if (!edges.iterator().hasNext()) { // root is a leaf
			return root;
		}
		for (Edge e : edges) {
			Vertex rv = e.getVertex(Direction.IN);
			if (IS.contains(getExpVertex(rv))) {
				Vertex result = querySubsetInTree(rv, IS);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private Vertex getExpVertex(Vertex ref) {
		SimpleProfiler.start("getExpVertex");
		//System.out.println("getExpVertex: "+ref.getProperty("name"));
		Iterable<Edge> edge = ref.getEdges(Direction.IN, EDGE_HAS_REF);
		Vertex expV = edge.iterator().next().getVertex(Direction.OUT);
		SimpleProfiler.stop("getExpVertex");
		return expV;
	}

	private Vertex addTreeNode(Vertex cur, Operation op, Vertex expV) {
		Iterable<Edge> refEdge = expV.getEdges(Direction.OUT, EDGE_HAS_REF);
		

		Vertex next = null;
		String expStr=op.toString();
		Iterable<Edge> edges = cur.getEdges(Direction.OUT, expStr);
		if (edges.iterator().hasNext()) {
			Edge e = edges.iterator().next();
			next = e.getVertex(Direction.IN);
		} else {
			next = graph.addVertex(null);
			next.setProperty("name", KEY_REF+expStr);
			expV.addEdge(EDGE_HAS_REF, next);
			cur.addEdge(EDGE_NEXT, next);
			cur.addEdge(expStr, next);
		}
		return next;
	}

	private Vertex updateIndexGraph(Operation op) {
		SimpleProfiler.start("updateIndexGraph-1");
		Vertex v = null;
		String s = op.toString();
		Vertex prefV = getPrefixVertex(op);
		SimpleProfiler.stop("updateIndexGraph-1");
		SimpleProfiler.start("updateIndexGraph-2");
		if (prefV == null) {
			prefV = graph.addVertex(null);
			Expression pref = LogicalRelationUtil.getPrefix(op);
			String ps = pref.toString();
			prefV.setProperty(PROP_NAME, KEY_PREF + ps);
			indexRoot.addEdge(ps, prefV);
		}
		SimpleProfiler.stop("updateIndexGraph-2");
		SimpleProfiler.start("updateIndexGraph-3");
		Iterable<Vertex> vertex = graph.getVertices(PROP_NAME, KEY_EXP + s);
		if (vertex.iterator().hasNext()) {
			v = vertex.iterator().next();
		} else {
			v = graph.addVertex(null);
			v.setProperty(PROP_NAME, KEY_EXP + s);
			v.setProperty(PROP_EXP, op);
			v.addEdge(EDGE_HAS_PREFIX, prefV);
			// insertIntoIPOG(prefV,v,op);
		}
		SimpleProfiler.stop("updateIndexGraph-3");
		return v;
	}

	// TODO do we need IPOG?
	// private void insertIntoIPOG(Vertex ipogVertex, Vertex v,Operation newOp)
	// {
	// //Operation op=ipogVertex.getProperty("exp");
	// Iterable<Edge> edges = ipogVertex.getEdges(Direction.OUT,"imply");
	// List<Edge> tobeRemove=new ArrayList<Edge>();
	// boolean inserted=false;
	// for(Edge e:edges){
	// Vertex sub = e.getVertex(Direction.IN);
	// Operation sop=sub.getProperty("exp");
	// if(LogicalRelationUtil.imply(sop, newOp)){
	// insertIntoIPOG(sub,v,newOp);
	// inserted=true;
	// }else if(LogicalRelationUtil.imply(newOp, sop)){
	// v.addEdge("imply", sub);
	// tobeRemove.add(e);
	// inserted=true;
	// }else{
	// insertIntoIPOG(sub,v,newOp);
	// }
	// }
	// for(Edge e:tobeRemove){
	// graph.removeEdge(e);
	// }
	// if(!inserted){
	// ipogVertex.addEdge("imply", v);
	// }
	// }

	public List<Vertex> getIS(Operation op) {
		SimpleProfiler.start("getIS");
		List<Vertex> vset = new ArrayList<Vertex>();
		Vertex prefv = getPrefixVertex(op);
		if (prefv!= null) {
			Iterable<Edge> edges = prefv.getEdges(Direction.IN, EDGE_HAS_PREFIX);
			for (Edge e : edges) {
				Vertex v = e.getVertex(Direction.OUT);
				Operation exp = (Operation) v.getProperty(PROP_EXP);
				if (op.equals(exp) || LogicalRelationUtil.imply(op, exp)) {
					vset.add(v);
				}
			}
		}
		SimpleProfiler.stop("getIS");
		return vset;
	}

	public List<Vertex> getRIS(Operation op) {
		SimpleProfiler.start("getRIS");
		List<Vertex> vset = new ArrayList<Vertex>();
		Vertex prefv = getPrefixVertex(op);
		if (prefv != null) {
			Iterable<Edge> edges = prefv.getEdges(Direction.IN, EDGE_HAS_PREFIX);
			for (Edge e : edges) {
				Vertex v = e.getVertex(Direction.OUT);
				Operation exp = (Operation) v.getProperty(PROP_EXP);
				if (op.equals(exp)) {
					vset.add(0, v);
				} else if (LogicalRelationUtil.imply(exp, op)) {
					vset.add(v);
				}
			}	
		}
		
		SimpleProfiler.stop("getRIS");
		return vset;
	}

//	private void putSubtreeIntoSet(Set<Vertex> vset, Vertex root) {
//		vset.add(root);
//		Iterable<Edge> edges = root.getEdges(Direction.OUT, EDGE_IMPLY);
//		for (Edge e : edges) {
//			putSubtreeIntoSet(vset, e.getVertex(Direction.IN));
//		}
//	}

//	private Vertex getExpVertex(Operation op) {
//		Iterable<Vertex> vertex = graph.getVertices(PROP_NAME, KEY_EXP + op.toString());
//		if (vertex.iterator().hasNext()) {
//			return vertex.iterator().next();
//		} else {
//			return null;
//		}
//	}

	private Vertex getPrefixVertex(Operation op) {
		Expression pref = LogicalRelationUtil.getPrefix(op);
		String ps = pref.toString();
		Iterable<Edge> edge = indexRoot.getEdges(Direction.OUT, ps);
		//Iterable<Vertex> prefs = graph.getVertices(PROP_NAME, KEY_PREF + ps);
		if (edge.iterator().hasNext()) {
			return edge.iterator().next().getVertex(Direction.IN);
		} else {
			return null;
		}
	}

	private void printGraph() {
		System.out.println("*******************\n");
		Iterator<Vertex> itr = graph.getVertices().iterator();
		while (itr.hasNext()) {
			Vertex v = itr.next();
			System.out.println("[" + v.getId() + "]");
			for (String key : v.getPropertyKeys()) {
				System.out.println("  " + key + ":" + v.getProperty(key));
			}

			// System.out.println(" exp:"+v.getProperty("exp"));
			Iterable<Edge> edges = v.getEdges(Direction.OUT);
			for (Edge e : edges) {
				System.out.println("  edge:" + e.getLabel() + " ->  " + e.getVertex(Direction.IN).getProperty("name"));
			}
		}
		System.out.println("*******************\n");
	}

}
