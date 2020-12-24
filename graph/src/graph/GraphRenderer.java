package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class GraphRenderer {
	HashMap<Integer, GraphNode> graph;
	
	public GraphRenderer(HashMap<Integer, GraphNode> g) {
		graph = g;
	}
	
	private String GetNodeSetStr() {
		String str;
		str = "var nodes = new vis.DataSet([";
		int n = 0;
		for (Entry<Integer, GraphNode> entry : graph.entrySet()) {
			if (n > 0) str += ","; ++n;
			str += "{id: " + entry.getKey() + ", label: '" + entry.getKey() + "'}";
		}
		
		str += "]);";
		return str;
	}
	
	private String GetEdgeSetStr() {
		String ret;
		ret =  "var edges = new vis.DataSet([";
		HashSet<GraphNode> nodesConsidered = new HashSet<GraphNode>();
		
		int e = 0;
		for (Entry<Integer, GraphNode> entry : graph.entrySet()) {
			for (Entry<GraphNode, Integer> reach :
					entry.getValue().GetReachables().entrySet()) {
				if (nodesConsidered.contains(reach.getKey())) continue;
				if (reach.getKey() == entry.getValue()) continue;
			
				if (e > 0) ret += ","; ++e;
				ret += "{from: " + entry.getKey() + 
						", to: " + reach.getKey().GetId() + 
						"}";
			}
			
			nodesConsidered.add(entry.getValue());
		}
			
		ret += "]);";
		return ret;
	}
	
	public String GetRenderStr() {
		String ret;
		ret = "\n<script type=\"text/javascript\" src=\"https://visjs.github.io/vis-network/standalone/umd/vis-network.min.js\"></script>" + "\n" + 
		      "<style type=\"text/css\">" + "\n" +
		       "#mynetwork {" + "\n" +
		       "width: 1200px;" + "\n" +
		       "height: 1200px;" + "\n" +
		       "border: 1px solid lightgray;" + "\n" +
		       "}" + "\n" +
		       "</style>" + "\n" +
		       "<div id=\"mynetwork\"></div>" + "\n" +
		       "<script type=\"text/javascript\">" + "\n" +
		       GetNodeSetStr() + "\n" +
		       GetEdgeSetStr() + "\n" +
		  "var container = document.getElementById('mynetwork');" + "\n" +
		  "var data = {" + "\n" +
		  "  nodes: nodes," + "\n" +
		  "  edges: edges" + "\n" +
		  "};" + "\n" +
		  "var options = {};" + "\n" +
		  "var network = new vis.Network(container, data, options);" + "\n" +
		"</script>";
		return ret;
	}
}
