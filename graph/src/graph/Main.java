package graph;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Main {

	public static void main(String[] args) throws IOException {
		System.out.println("Hello Graph World!!!");
		HttpServer server = null;
		
		try {
			server = HttpServer.create(new InetSocketAddress(9500), 0);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpContext context = server.createContext("/");
		context.setHandler(exchange -> {
			handleRequest(exchange);
		});
		
		server.start();
	}

	private static String getHeader() {
        return "<head><style>\n" + 
    	      "table, th, td {\n" + 
    	      "  border: 1px solid black;\n" + 
    	      "}\n" + 
    	      "</style></head>";
	}
	
	private static void handleRequest(HttpExchange exchange) throws IOException {
		System.out.println("Handle request...");
		OutputStream os = exchange.getResponseBody();
		
		URI requestedUri = exchange.getRequestURI();
		String query = requestedUri.getRawQuery();
        System.out.println(query);
        boolean new_graph = false;
        int from_id = -1;
        int to_id = -1;
        boolean solve = false;
	
        if (query != null) {
        	StringTokenizer tokenizer = new StringTokenizer(query, "&");
        	while (tokenizer.hasMoreTokens()) {
        		String param = tokenizer.nextToken();
        		StringTokenizer param_tokenizer = new StringTokenizer(param, "=");
        		String param_name = param_tokenizer.nextToken();
        		
        		switch (param_name) {
        		case "new_graph":
        			param_tokenizer.nextToken();
        			System.out.println("new graph");
        			new_graph = true;
        			break;
        		case "solve":
        			param_tokenizer.nextToken();
        			System.out.println("solve");
        			solve = true;
        			break;
        		case "from_id":
        			try {
        				from_id = Integer.parseInt(param_tokenizer.nextToken());
        				System.out.println("From: " + from_id);
        			} catch (Exception e) {
        				from_id = -1;
        				System.err.println(e.getMessage());
        			}
        			break;
        		case "to_id":
        			try {
        				to_id = Integer.parseInt(param_tokenizer.nextToken());
        				System.out.println("To: " + to_id);
        			} catch (Exception e) {
        				to_id = -1;
        				System.err.println(e.getMessage());
        			}
        			break;
        		}
        	}
        }
        
		String body = "<html>" + getHeader();
		body += generateBanner();
	    body += "<table style=\"border:0;width:100%\">\n";
	    body += "<tr>";
	    body += generateLinks();
	    body += "</tr></table></br>";
	    
	    HashMap<Integer, GraphNode> graph = new HashMap<Integer, GraphNode>();
	    String cookie_val = "";
	    if (!new_graph) {
	    	System.out.println("building graph.");
	    	cookie_val = buildGraph(exchange, new_graph, graph, from_id, to_id);
	    }
	    
	    body += "<table style=\"border:0;width:100%\"><tr><td style=\"border:0;width:50%\">";
	    body += generateGraph(graph);
	    body += "</td>";
	    body += "<td style=\"border:0;width:100%\">";
	    body += getSolutionStr(graph);
	    body += "</td>";
	    body += "</tr></table></br></body></html>";
	    
	    if (cookie_val.isEmpty()) {
			cookie_val = "path_list=";
		}
	    
	    List<String> new_cookies = new ArrayList<>();		
		new_cookies.add(cookie_val);
		System.out.println("cookie: " + cookie_val);
		
		Headers respHeaders = exchange.getResponseHeaders();
		respHeaders.put("Set-Cookie", new_cookies);
	    exchange.sendResponseHeaders(200, body.getBytes().length);//response code and length
	    os.write(body.getBytes());
	    os.close();
	}

	private static String getSolutionStr(HashMap<Integer, GraphNode> graph) {
		if (graph.size() == 0) {
			return new String();
		}
		
		HashMap<Integer, GraphNode> solution =
				AllPairsShortestPaths.Compute(graph);
		
		int max_node_id = 1;
		for (Entry<Integer, GraphNode> entry : solution.entrySet()) {
			if (entry.getKey() > max_node_id) {
				max_node_id = entry.getKey();
			}
		}
		
		HashMap<Integer, HashMap<Integer, Integer>> pair_map =
				new HashMap<Integer, HashMap<Integer, Integer>>();
		
		for (Entry<Integer, GraphNode> entry : solution.entrySet()) {
			HashMap<Integer, Integer> entryMap = pair_map.getOrDefault(entry.getKey(),
					   new HashMap<Integer, Integer>());
			for ( Entry<GraphNode, Integer> nodeEntry : entry.getValue().GetReachables().entrySet()) {
				entryMap.put(nodeEntry.getKey().GetId(), nodeEntry.getValue());
			}
			
			pair_map.put(entry.getKey(), entryMap);
		}
		
		String solution_str = "<p1>Solution</p1></br><table>";
		
		// 1. Header
		solution_str += "<tr>";
		for (int i = 0; i <= max_node_id; ++i) {
			solution_str += "<td style=\"width:30px\">";
			if (i != 0) {
				solution_str += i;
			}
			solution_str += "</td>";
		}
		
		solution_str += "</tr>";
		// 2. Per row.
		for (int i = 1; i <= max_node_id; ++i) {
			solution_str += "<tr><td>" + i + "</td>";
			HashMap<Integer, Integer> entryMap = pair_map.getOrDefault(i, new HashMap<Integer, Integer>());
			for (int j = 1; j <= max_node_id; ++j) {
				int len = entryMap.getOrDefault(j, Integer.MAX_VALUE);
				String len_str = (len < Integer.MAX_VALUE) ? Integer.toString(len) : "INF";
				solution_str += "<td>" + len_str + "</td>";
			}
			
			solution_str += "</tr>";
		}
		
		solution_str += "</table>";
		
		System.out.println(solution_str);
		
		// TODO Auto-generated method stub
		return solution_str;
	}

	private static String generateGraph(HashMap<Integer, GraphNode> graph) {
		return new GraphRenderer(graph).GetRenderStr();
	}

	private static String generateBanner() {
		return "<h1 style=\"text-align:center\">Welcome to Graph Solver!</h1></br>";
	}
	
	private static String buildGraph(HttpExchange exchange, boolean new_graph, HashMap<Integer, GraphNode> graph, int from, int to) {
		String cookie_val = "";
		
		if (from > 0) {
			updateGraph(graph, from, to);
		}
		
		Headers reqHeaders = exchange.getRequestHeaders();
		List<String> cookies = reqHeaders.get("Cookie");
	
		if (!new_graph && cookies != null) {
			for (String cookie: cookies) {
				if (!cookie.contains("path_list")) {
					continue;
				}
				
				cookie_val = cookie;
				StringTokenizer tokenizer = new StringTokenizer(cookie, "=,");
				tokenizer.nextToken();
				while (tokenizer.hasMoreTokens()) {
					String pair = tokenizer.nextToken();
					StringTokenizer ptokens = new StringTokenizer(pair, ":");
					int from_id = Integer.parseInt(ptokens.nextToken());
					int to_id = Integer.parseInt(ptokens.nextToken());
					updateGraph(graph, from_id, to_id);
				}
			}
		}
		
		if (from > 0) {
			if (cookie_val.isEmpty()) {
				cookie_val = "path_list=";
			} else if (!cookie_val.equals("path_list=")){
				cookie_val += ",";
				
			}
			
			cookie_val = cookie_val + from + ":" + to;
		}
		
		return cookie_val;
	}
	
	private static void updateGraph(HashMap<Integer, GraphNode> graph, int from_id, int to_id) {
		System.out.println("Updating: " + from_id + " -> " + to_id);
		GraphNode fromNode = null, toNode = null;
		if (!graph.containsKey(from_id)) {
			fromNode = new GraphNode(from_id);
			graph.put(from_id, fromNode);
		}
		
		if (!graph.containsKey(to_id)) {
			toNode = new GraphNode(to_id);
			graph.put(to_id, toNode);
		}
		
		fromNode = graph.get(from_id);
		toNode = graph.get(to_id);
		
		fromNode.AddReachable(toNode, 1);
		toNode.AddReachable(fromNode, 1);
	}

	private static String generateLinks() {
		String body = "";
		body += "<td style=\"border:0;text-align:center\"><a href=\"?new_graph=true\">New Graph</a></td>";
		//body += "<td style=\"border:0;text-align:center\"><a href=\"?solve=true\">Solve</a></td>";
		body += "<td style=\"border:0;text-align:center\"><form><input type=\"text\" id=\"from_id\" name=\"from_id\" value=\"From\">";
		body += "<input type=\"text\" id=\"to_id\" name=\"to_id\" value=\"To\">";
		body += "<input type=\"submit\" value=\"Add New Path\"></td>";
		return body;
	}

}
