package housie;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello World");
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(8500), 0);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpContext context = server.createContext("/");
		context.setHandler(exchange -> {
			try {
				handleRequest(exchange);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		});
		
		HttpContext context2 = server.createContext("/get_tickets");
		context2.setHandler(exchange -> {
			try {
				handleGetTicketsRequest(exchange);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
			}
		});
		
		HttpContext context3 = server.createContext("/grid");
		context3.setHandler(exchange -> {
			try {
				handleGridRequest(exchange);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
			}
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
	
	private static List<Integer> generateShuffledList(int lower_bound, int upper_bound, Set<Integer> exclude_list, boolean should_sort) {
		List<Integer> numbers = new ArrayList<Integer>();
		for (int i = lower_bound; i <= upper_bound; ++i) {
			if (exclude_list.contains(i)) {
				continue;
			}
			
		    numbers.add(i);
		}
		
		Collections.shuffle(numbers);
		
		//System.out.print("Shuffled numbers: ");
		//for (int i = 0; i <= 2; ++i) {
		//	System.out.print("[" + numbers.get(i) + "] ");
		//}
		
		//System.out.println();
		if (should_sort) {
			for (int i = 0; i < 2; ++i) {
				int idx = i;
				for (int j = i + 1; j <= 2; ++j) {
					if (numbers.get(j) < numbers.get(idx)) {
						idx = j;
					}
				}
				
				if (idx != i) {
					int tmp = numbers.get(idx);
					numbers.set(idx, numbers.get(i));
					numbers.set(i, tmp);
				}
			}
		}
		return numbers;
	}
	
	private static Vector<Integer> generateRandomTicketNumbers() {
		Vector<Integer> ticket = new Vector<Integer>();
		for (int i = 0; i < 90; ++i) ticket.add(0);
		
		List<List<Integer>> numbers = new ArrayList<List<Integer>>();
		
		for (int col = 0; col < 9; ++col) {
			int lower_bound = col == 0 ? 1 : col * 10;
			int upper_bound = col != 8 ? (col + 1) * 10 - 1 : (col + 1) * 10;
		    numbers.add(generateShuffledList(lower_bound, upper_bound, new HashSet<Integer>(), true));
		    //System.out.print("[" + col + "] -> ");
		    //for (int number : numbers.get(col)) {
		    //	System.out.print(number + " ");
		    //}
		    //System.out.println();
		}
		
		Map<Integer, Integer> level_num_map = new HashMap<Integer, Integer>();
		level_num_map.put(0, 0);
		level_num_map.put(1, 0);
		level_num_map.put(2, 0);
		//System.out.println("Start");

		// Generate 1 number per column.
		Random rand = new Random();
		for (int col = 0; col < 9; ++col) {
			int level = rand.nextInt(1000) % 3;
			if (level_num_map.get(level) == 5) {
				--col;
				continue;
			}
			
			level_num_map.put(level, level_num_map.get(level) + 1);
			int index = 9 * level +  col;
			int num = numbers.get(col).get(level);
			numbers.get(col).set(level, 0);
			System.out.println("- col: " + col + " num: " + num);
			ticket.set(index, num);
		}
				
		// Generate remaining numbers in each level.
		for (int i = 0; i < 3; ++i) {
			while (level_num_map.get(i) < 5) {
				int pos = rand.nextInt(1000) % (9 - level_num_map.get(i));
				int index = 9 * i;
				int j = 0;
				while (j <= pos) {
					if (ticket.get(index) == 0) {
						if (j == pos) {
							int num = numbers.get(index % 9).get(i);
							numbers.get(index % 9).set(i,  0);
							System.out.println("col: " + (index % 9) + " num: " + num);
							ticket.set(index, num);
							break;
						}
						
						++j;
					}
					
					++index;
				}
				
				level_num_map.put(i, level_num_map.get(i) + 1);
			}
		}
		
		return ticket;
	}
	
	private static void handleGetTicketsRequest(HttpExchange exchange) throws IOException {
		// parse request
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        System.out.println(query);
        int num_tickets = 1;
        
        if (query != null && query.contains("n=")) {
        	StringTokenizer tokenizer = new StringTokenizer(query, "=");
        	tokenizer.nextToken();
        	num_tickets = Integer.parseInt(tokenizer.nextToken());
        }

        if (num_tickets > 10) {
        	num_tickets = 10;
        }
        
        OutputStream os = exchange.getResponseBody();
		  
        String body = "<html>";// + getHeader();
		body += "<body></br>";
		
		for (int nT = 0; nT < num_tickets; ++nT) {
			body += "<table>";
			Vector<Integer> ticket = null;
			  
			try {
				ticket = generateRandomTicketNumbers();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		  
			for (int i = 0; i < 3; ++i) {
				body += "<tr>";
				for (int j = 0; j < 9; ++j) {
					int index = 9 * i + j;
					if (ticket.get(index) == 0) {
						body += "<td style=\"width:10%\"></td>";
					} else {
						body += "<td style=\"width:10%\">" + ticket.get(index) + "</td>";
					}
				}
				body += "</tr>";
			}
			body += "</table></br>";
		 }
		  
		  body += "</body></html>";
	      exchange.sendResponseHeaders(200, body.getBytes().length);//response code and length
	      os.write(body.getBytes());
	      os.close();
	}

	private static void handleGridRequest(HttpExchange exchange) throws IOException {
		Headers reqHeaders = exchange.getRequestHeaders();
		Headers respHeaders = exchange.getResponseHeaders();
		List<String> cookies = reqHeaders.get("Cookie");
		Set<Integer> exclude_list = new HashSet<Integer>();
		
		String body = "<html>" + getHeader() + "\nFinished list: ";
		
		if (cookies != null) {
			for (String cookie: cookies) {
				System.out.println(cookie);
				if (!cookie.contains("exclude_list")) {
					continue;
				}
				
				StringTokenizer tokenizer = new StringTokenizer(cookie, "=,");
				
				System.out.println(tokenizer.nextToken());
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					System.out.println(token);
					body += token + "  ";
					exclude_list.add(Integer.parseInt(token));
				}
			}
		}

		body += "</br>";
		int num = 0;

		if (exclude_list.size() != 90) {
			while (true) {
				Random rand = new Random();
				num = rand.nextInt(90) + 1;
				if (!exclude_list.contains(num)) {
					exclude_list.add(num);
					System.out.println(num);
					break;
				}
			}
		}
		
		//new_cookies.add(Integer.toString(num));
		List<String> new_cookies = new ArrayList<>();
		String cookie_val = "";
		for (Integer id : exclude_list) {
			if (cookie_val != "") {
				cookie_val += ",";
			}
			cookie_val += Integer.toString(id);
		}
		
		new_cookies.add("exclude_list=" + cookie_val);
		System.out.println(cookie_val);
		
		respHeaders.put("Set-Cookie", new_cookies);
		body += "Num: " + num + "</br>";
		
		body += "<table>";
		for (int i = 0; i < 9; ++i) {
			body += "<tr>";
			for (int j = 1; j <= 10; ++j) {
				int grid_num = i * 10 + j;
				if (exclude_list.contains(grid_num)) {
					body += "<td style=\"width:10%;background-color:#00FF00\">";
				} else {
					body += "<td>";
				}
				
				body += Integer.toString(grid_num) + "</td>";
			}
			body += "</tr>";
		}
		
		body += "</table></html>";
		OutputStream os = exchange.getResponseBody();
		exchange.sendResponseHeaders(200, body.getBytes().length);//response code and length
		os.write(body.getBytes());
		os.close();
	}

	private static void handleRequest(HttpExchange exchange) throws IOException {
		OutputStream os = exchange.getResponseBody();
	      
		URI requestedUri = exchange.getRequestURI();
		String query = requestedUri.getRawQuery();
        System.out.println(query);
        boolean next_number = false;
        boolean new_game = false;
        int num_tickets = 0;
        boolean play_audio = false;
                
        if (query != null) {
        	StringTokenizer tokenizer = new StringTokenizer(query, "&");
        	while (tokenizer.hasMoreTokens()) {
        		String param = tokenizer.nextToken();
        		StringTokenizer param_tokenizer = new StringTokenizer(param, "=");
        		String param_name = param_tokenizer.nextToken();
        		
        		switch (param_name) {
	        		case "next_number":
	        			param_tokenizer.nextToken();
	        			next_number = true;
	        			break;
        			case "new_game":
	        			param_tokenizer.nextToken();
	        			new_game = true;
	        			break;
	        		case "num_tickets":
	        			try {
	        				num_tickets = Integer.parseInt(param_tokenizer.nextToken());
	        				if (num_tickets > 5) {
	        					num_tickets = 5;
	        				}
	        			} catch (Exception e) {
	        				num_tickets = 0;
	        				System.err.println(e.getMessage());
	        			}
	        			break;
	        		case "audio":
	        			param_tokenizer.nextToken();
	        			play_audio = true;
	        			System.out.println("audio");
	        			break;
	        		default:
        		}
        	}
        }
        
      String body = "<html>" + getHeader();
      
      do {
	      if (play_audio) {
	    	  body += generateAudioPlayer();
	    	  break;
	      }
	      
	      body += generateBanner();
	      body += "<table style=\"border:0;width:100%\">\n";
	      body += "<tr>";
	      body += generateLinks();
	      body += "</tr></table>";
	      body += "<table style=\"width:100%\">\n";
	      body += "<tr><td style=\"border:0;text-align:center;width:50%\">";
	      body += generateGrid(exchange, next_number, new_game);
	      body += "</td><td style=\"border:0;text-align:center\">";
	      for (int i = 0; i < num_tickets; ++i) {
	    	  body += "</br>" + generateTicket() + "</br>";
	      }
	      
	      body += "</td></tr>";
	      body += "</table>";
      } while (false);
      
      body += "</html>";
      exchange.sendResponseHeaders(200, body.getBytes().length);//response code and length
      os.write(body.getBytes());
      os.close();
  }


	private static String generateAudioPlayer() {
		String body = "";
		body += "<audio controls>";
		body += "<source src=\"horse.mp3\" type=\"audio/mpeg\">";
		body += "Your browser does not support the audio element.";
		body += "</audio>"; 
		return body;
	}


	private static String generateTicket() {
		String body = "<table style=\"margin-left:10%\">";
		Vector<Integer> ticket = null;
		  
		try {
			ticket = generateRandomTicketNumbers();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	  
		for (int i = 0; i < 3; ++i) {
			body += "<tr>";
			for (int j = 0; j < 9; ++j) {
				int index = 9 * i + j;
				if (ticket.get(index) == 0) {
					body += "<td style=\"text-align:center;width:10%\"></td>";
				} else {
					body += "<td style=\"text-align:center;width:10%\">" + ticket.get(index) + "</td>";
				}
			}
			body += "</tr>";
		}
		body += "</table></br>";
		return body;
	}


	private static String generateGrid(HttpExchange exchange, boolean next_number, boolean new_game) {
		Set<Integer> exclude_list = new HashSet<Integer>();
		
		if (!new_game) {
			Headers reqHeaders = exchange.getRequestHeaders();
			Headers respHeaders = exchange.getResponseHeaders();
			List<String> cookies = reqHeaders.get("Cookie");
		
			if (cookies != null) {
				for (String cookie: cookies) {
					//System.out.println("Cookie: " + cookie);
					if (!cookie.contains("exclude_list")) {
						continue;
					}
					
					StringTokenizer tokenizer = new StringTokenizer(cookie, "=,");
					tokenizer.nextToken();
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						System.out.println("exclude=" + token);
						exclude_list.add(Integer.parseInt(token));
					}
				}
			}
		}


		int num = 0;
		if (next_number) {
			List<Integer> new_list = generateShuffledList(1, 90, exclude_list, false);
			if (new_list.size() > 0) {
				num = new_list.remove(0);
			}
		}
		
		//System.out.println("num generated " + num);
		exclude_list.add(num);
		String body = "</br></br></br>";
		
		List<String> new_cookies = new ArrayList<>();
		String cookie_val = "";
		for (Integer id : exclude_list) {
			if (cookie_val != "") {
				cookie_val += ",";
			}
			cookie_val += Integer.toString(id);
		}
		
		new_cookies.add("exclude_list=" + cookie_val);
		System.out.println(cookie_val);
		
		Headers respHeaders = exchange.getResponseHeaders();
		respHeaders.put("Set-Cookie", new_cookies);
		if (num != 0) {
			body += "New Number: " + num;
		}
		
		body += "</br><table style=\"margin-left:25%\">";
		for (int i = 0; i < 9; ++i) {
			body += "<tr>";
			for (int j = 1; j <= 10; ++j) {
				int grid_num = i * 10 + j;
				if (exclude_list.contains(grid_num)) {
					body += "<td style=\"text-align:center;width:10%;background-color:#00FF00\">";
				} else {
					body += "<td style=\"text-align:center\">";
				}
				
				body += Integer.toString(grid_num) + "</td>";
			}
			body += "</tr>";
		}
		
		body += "</table></br></br>";
		return body;
	}

	private static String generateBanner() {
		return "<h1 style=\"text-align:center\">Welcome to Evergreen Housie!</h1></br>";
	}
	
	private static String generateLinks() {
		String body = "";
		body += "<td style=\"border:0;width=33%\"><a href=\"?next_number=true\">Next Number</a></td>";
		body += "<td style=\"border:0;width=33%;text-align:center\"><a href=\"?new_game=true\">New Game</a></td>";
		body += "<td style=\"border:0;width=400px;text-align:right\"><form style=\"width=33%\" action=\"/\"><input type=\"submit\" value=\"Get New Tickets\">";
		body += "<input type=\"text\" id=\"num_tickets\" name=\"num_tickets\"></form></td>";
		return body;
	}

}
