package graphdb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.lambdazen.bitsy.BitsyGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;

import cn.edu.whu.sklse.greentrie.store.LTrie;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;

public class BitsyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Path dbPath = Paths.get("./store");
		BitsyGraph graph = new BitsyGraph(dbPath);

		// LTrie ltrie=new LTrie("./store/sat");
		List<Operation> opList = new ArrayList<Operation>();
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v1, v2);
		Operation o2 = new Operation(Operation.Operator.SUB, v2, v1);
		Operation o3 = new Operation(Operation.Operator.LT, o1, o2);
		opList.add(o1);
		opList.add(o2);
		opList.add(o3);
		Set<String> keys = graph.getIndexedKeys(Vertex.class);
		if(keys.isEmpty()){
			graph.createKeyIndex("exp", Vertex.class);
		}
		Vertex father=null;
		Vertex root=null;
		for (Operation op : opList) {
			Iterable<Vertex> vs = graph.getVertices("exp", op.toString());
			Vertex v=null;
			if(vs.iterator().hasNext()){
				v=vs.iterator().next();
			}else{
				v = graph.addVertex(null);
				v.setProperty("exp", op.toString());
			}
			if(father!=null){
				father.addEdge("next", v);
			}else{
				root=v;
			}
			father=v;
		}
		

		long ts = System.currentTimeMillis();
		// for (int i = 0; i < 20; i++) {
		// Vertex v = graph.addVertex("node"+i);
		// v.setProperty("rand", Math.random());
		// v.setProperty("count", i);
		//
		// if (i % 20 == 0) {
		// graph.commit();
		// }
		//
		// // Make sure the vertex is there in the middle of the Tx
		// //assertEquals(new Integer(i),
		// graph.getVertex(vids[i]).getProperty("count"));
		// }
		// graph.commit();
		Vertex c=root;
		while(c!=null){
			System.out.println(c.getProperty("exp"));
			Iterable<Edge> nextE = c.getEdges(Direction.OUT, "next");
			if(nextE.iterator().hasNext()){
				c=nextE.iterator().next().getVertex(Direction.IN);
			}else{
				c=null;
			}
		}
		
		graph.commit();
//		
//		Iterator<Vertex> itr = graph.getVertices().iterator();
//		while (itr.hasNext()) {
//			
//		}
		graph.flushTxLog();

		// Close the database
		graph.shutdown();

	}

}
