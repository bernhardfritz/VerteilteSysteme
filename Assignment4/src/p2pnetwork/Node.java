package p2pnetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Node extends Thread {
	private ServerSocket serverSocket;
	private Set<ShallowNode> nodeTable;
	private ShallowNode self;
	private int n; 
	private boolean connected;
	
	public Node(int n) {
		super(Node.getRandomAlphaNumericString(10));
		this.n = n;
		connected = false;
		nodeTable = Collections.newSetFromMap(new ConcurrentHashMap<ShallowNode, Boolean>()); // concurrent set of known nodes
		try {
			serverSocket = new ServerSocket(0); // ServerSocket instance bound to some arbitrary port
			serverSocket.setSoTimeout(3000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		self = new ShallowNode(getIp(), getPort(), getName());
	}
	
	public Node(int n, ShallowNode otherNode) {
		this(n);
		if(otherNode != null) this.nodeTable.add(otherNode);
	}
	
	public String getIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress(); // serverSocket is not used here since serverSocket address will always be 0.0.0.0
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
	
	public void connect() {
		connected = true;
		System.out.printf("Node %s has connected to the p2p network\n", self);
	}
	
	public void disconnect() {
		connected = false;
		System.out.printf("Node %s has disconnected from the p2p network\n", self);
	}
	
	private ShallowNode pickRandomNodeFromTable() {
		if(nodeTable.isEmpty()) return null;
		Random r = new Random();
		return new ArrayList<ShallowNode>(nodeTable).get(r.nextInt(nodeTable.size())); // set does not provide .get() function
	}
	
	private void send(Socket socket, String json) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String recv(Socket socket) {
		String json = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			json = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	private void trimNodeTable() {
		while(nodeTable.size() > n) {
			nodeTable.remove(pickRandomNodeFromTable());
		}
	}
	
	private static List<Character> characters = new ArrayList<Character>();
	static {
		for(char c = 'a'; c <= 'z'; c++) characters.add(c);
		for(char c = '0'; c <= '9'; c++) characters.add(c);
	}
	
	private static String getRandomAlphaNumericString(int length) {
		if(length <= 0) return "";
		return characters.get(new Random().nextInt(characters.size())).toString() + getRandomAlphaNumericString(length-1);
	}
	
	private ShallowNode getSender(String ip, int port, Set<ShallowNode> otherNodes) {
		for(ShallowNode shallowNode : otherNodes) {
			boolean sameIp = ip.equals(shallowNode.getIp());
			boolean samePort = port == shallowNode.getPort();
			if(sameIp && samePort) return shallowNode;
		}
		System.out.printf("no match found for %s:%d in table %s\n", ip, port, otherNodes);
		return null;
	}
	
	@Override
	public void run() {
		connect();
		// anonymous thread opens a socket connection to a random node from node table every 5 seconds, 
		// sends the whole node table including its own information,
		// receives the node table from the random node using the same socket connection,
		// 'merges' both tables
		// and trims the table to contain a total of n entries
		new Thread() {
			public void run() {
				while(connected) {
					ShallowNode randomNode = pickRandomNodeFromTable();
					if(randomNode != null) {
						Socket socket = null;
						try {
							socket = new Socket(randomNode.getIp(), randomNode.getPort());
							nodeTable.add(self);
							send(socket, new Gson().toJson(nodeTable));
							System.out.printf("Node %s sent its node table consisting of nodes %s to node %s\n", self, nodeTable, randomNode);
							nodeTable.remove(self);
							String json = recv(socket);
							Type setType = new TypeToken<HashSet<ShallowNode>>() {}.getType();
							Set<ShallowNode> otherNodes = new Gson().fromJson(json, setType);
							System.out.printf("Node %s received a node table consisting of nodes %s from node %s\n", self, otherNodes, randomNode);
							nodeTable.addAll(otherNodes);
							nodeTable.remove(self);
							trimNodeTable();
							System.out.printf("After merging and trimming the new node table of Node %s looks like %s\n", self, nodeTable);
						} catch (UnknownHostException e) {
							System.out.printf("Node %s failed to send its node table to node %s. Therefore it will be removed from the sender's node table", self, randomNode);
							nodeTable.remove(randomNode); // remove entry from node table if it cannot be reached
							continue; 					  // and pick another entry until no further entry is left
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							try {
								if(socket != null) socket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		// main thread waits for new connections.
		// once a connection is established, it receives the node table from the other node,
		// sends its own (unmerged) table including its own information using the same socket connection.
		// 'merges' the both tables,
		// and trims the table to contain a total of n entries
		while(connected) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				String json = recv(socket);
				Type setType = new TypeToken<HashSet<ShallowNode>>() {}.getType();
				Set<ShallowNode> otherNodes = new Gson().fromJson(json, setType);
				ShallowNode other = getSender(socket.getLocalAddress().getHostAddress(), socket.getLocalPort(), otherNodes); // this shallow node is for sysout reasons only
				System.out.printf("Node %s received a node table consisting of nodes %s from %s\n", self, otherNodes, other);
				nodeTable.add(self);
				send(socket, new Gson().toJson(nodeTable));
				System.out.printf("Node %s sent its node table consisting of nodes %s to node %s\n", self, nodeTable, other);
				nodeTable.remove(self);
				nodeTable.addAll(otherNodes);
				nodeTable.remove(self);
				trimNodeTable();
				System.out.printf("After merging and trimming, the new node table of Node %s looks like %s\n", self, nodeTable);
				
			} catch(SocketTimeoutException e) {
				System.out.printf("Node %s socket timed out waiting for new connections. Trying again...\n", self);
				continue;
			} catch (IOException e) { 
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if(socket != null) socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		final int n = 3;
		// creating a network of 3*n nodes
		Node firstPeer = new Node(n);
		List<Node> peers = new ArrayList<Node>();
		peers.add(firstPeer);
		for(int i = 1; i < 9; i++) {
			Node prev = peers.get(i-1);
			peers.add(new Node(n, new ShallowNode(prev.getIp(), prev.getPort(), prev.getName())));
		}
		for(Thread t : peers) {
			t.start();
		}
		// every second one node will be disconnected from the p2p network
		for(Node node : peers) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			node.disconnect();
		}
		// wait for all threads to be finished
		for(Thread t : peers) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
