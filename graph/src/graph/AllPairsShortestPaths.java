package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

public class AllPairsShortestPaths {

	public static HashMap<Integer, GraphNode> Compute(HashMap<Integer, GraphNode> graph) {
		HashMap<Integer, GraphNode> shortestPaths = new HashMap<Integer, GraphNode>();
		for (Entry<Integer, GraphNode> entry : graph.entrySet()) {
			GraphNode sNode = shortestPaths.get(entry.getKey());
			if (sNode == null) {
				sNode = new GraphNode(entry.getKey());
				shortestPaths.put(sNode.GetId(), sNode);
			}
			
			Compute(sNode, graph, shortestPaths);
		}
		// TODO Auto-generated method stub
		return shortestPaths;
	}

	private static void Compute(GraphNode sNode,
			                    HashMap<Integer, GraphNode> graph,
			                    HashMap<Integer, GraphNode> shortestPaths) {
		HashSet<GraphNode> consideredSet = new HashSet<GraphNode>();
		Queue<GraphNode> reachables = new LinkedList<GraphNode>();
		Queue<Integer> reach_len = new LinkedList<Integer>();
		reachables.add(sNode);
		consideredSet.add(sNode);
		reach_len.add(0);
		
		while (reachables.size() > 0) {
			GraphNode node = reachables.remove();
			int len = reach_len.remove();
			
			GraphNode gNode = graph.get(node.GetId());
			for (Entry<GraphNode, Integer> entry : gNode.GetReachables().entrySet()) {
				if (entry.getKey() == gNode) continue;
				
				GraphNode spNode = shortestPaths.get(entry.getKey().GetId());
				if (spNode == null) {
					spNode = new GraphNode(entry.getKey().GetId());
					shortestPaths.put(spNode.GetId(), spNode);
				}
				
				if (!sNode.GetReachables().containsKey(spNode)) {
					sNode.AddReachable(spNode, len + 1);
					spNode.AddReachable(sNode, len + 1);
				}
				
				if (!consideredSet.contains(spNode)) {
					consideredSet.add(spNode);
					reachables.add(spNode);
					reach_len.add(len + 1);
				}
			}
		}
	}
}
