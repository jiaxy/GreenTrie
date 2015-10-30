package graphdb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import com.lambdazen.bitsy.BitsyGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;

public class BitsyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Path dbPath = Paths.get("./store");
		  BitsyGraph graph = new BitsyGraph(dbPath);
//		  long ts = System.currentTimeMillis();
//          for (int i = 0; i < 20; i++) {
//              Vertex v = graph.addVertex(null);
//              v.setProperty("rand", Math.random());
//              v.setProperty("count", i);
//
//              if (i % 20 == 0) {
//                  graph.commit();
//              }
//
//              // Make sure the vertex is there in the middle of the Tx
//              //assertEquals(new Integer(i), graph.getVertex(vids[i]).getProperty("count"));
//          }
//          graph.commit();
          Iterator<Vertex> itr = graph.getVertices().iterator();
         while(itr.hasNext()){
        	 System.out.println(itr.next().getProperty("rand"));
         }
        
	        // Close the database
	        graph.shutdown();

	}

}
