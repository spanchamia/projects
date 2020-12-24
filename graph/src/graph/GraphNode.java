package graph;

import java.util.HashMap;
import java.util.Map.Entry;

public class GraphNode {
	private int id;
	private HashMap<GraphNode, Integer> reachable;
	
	public GraphNode(int i) {
		id = i;
		reachable = new HashMap<GraphNode, Integer>();
		AddReachable(this,  0);
	}

	public int GetId() {
		return id;
	}
	
	public void AddReachable(GraphNode n, int len) {
		reachable.put(n, len);
	}
	
	public HashMap<GraphNode, Integer> GetReachables() {
		return reachable;
	}

	public void PrintReachables() {
		for (Entry<GraphNode, Integer> entry : reachable.entrySet()) {
			if (entry.getKey().GetId() == GetId()) continue;
			
			System.out.print(" [Node: " + entry.getKey().GetId() +
					         " Len: " + entry.getValue() + "]");
		}
		
	}

	public String GetReachablesStr() {
		String reachable_str = "";
		for (Entry<GraphNode, Integer> entry : reachable.entrySet()) {
			if (entry.getKey().GetId() == GetId()) continue;
			
			reachable_str += " [Node: " + entry.getKey().GetId() +
					         " Len: " + entry.getValue() + "]";
		}
		
		return reachable_str;
	}
	
	@Override
	public String toString() {
		return "ID: " + GetId();
		
	}
}
