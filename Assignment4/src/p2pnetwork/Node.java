package p2pnetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Node extends Thread {
	private ServerSocket serverSocket;
	private Set<ShallowNode> nodeTable;
	private ShallowNode self;
	private int n;
	private int hopCount;
	private boolean connected;
	
	private ShallowNode requestedNode;
	
	public ShallowNode getRequestedNode() {
		return requestedNode;
	}

	public void setRequestedNode(ShallowNode requestedNode) {
		this.requestedNode = requestedNode;
	}

	public Node(String name, int n) {
//		super(Node.getRandomAlphaNumericString(10));
		super(name);
		this.n = n;
		this.hopCount = 2*n;
		connected = false;
		nodeTable = Collections.newSetFromMap(new ConcurrentHashMap<ShallowNode, Boolean>()); // concurrent set of known nodes
		try {
			serverSocket = new ServerSocket(0); // ServerSocket instance bound to some arbitrary port
			serverSocket.setSoTimeout(3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		self = new ShallowNode(getIp(), getPort(), getName());
	}
	
	public Node(String name, int n, ShallowNode otherNode) {
		this(name, n);
		if(otherNode != null) this.nodeTable.add(otherNode);
	}
	
	public String getIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress(); // serverSocket is not used here since serverSocket address will always be 0.0.0.0
		} catch (UnknownHostException e) {
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
			e.printStackTrace();
		}
	}
	
	private String recv(Socket socket) {
		String json = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			json = in.readLine();
		} catch (SocketException e) {
			return json;
		} catch (IOException e) {
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
	
	public void sendOneToAllMessage(String text) {
		Message msg = new Message(self, hopCount, text, null);
		for (ShallowNode shallowNode : nodeTable) {
			sendMessage(shallowNode, msg);
		}
	}
	
	public ShallowNode requestNodeByName(String name){
		
		//create message for node request
		Message msg = new Message(self, hopCount, null, name);
		
		//send message to all known nodes
		for (ShallowNode shallowNode : nodeTable) {
			sendMessage(shallowNode, msg);
		}
		
		//wait max. 10 seconds
		int count = 0;
		while(getRequestedNode() == null && count < 10){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count++;
		}
		
		//return node
		return getRequestedNode();
		
	}
	
	private void sendMessage(ShallowNode recipient, Message msg) {
		if (connected) {
			Socket socket = null;
			try {
				socket = new Socket(recipient.getIp(), recipient.getPort());
				send(socket, new Gson().toJson(msg));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				return;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(socket != null) socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void handleOneToAllMessageReception(Message msg) {
		System.out.printf("Node %s received a one-to-all message('%s') from %s\n", self, msg.getText(), msg.getSourceNode());
		msg.decHopCount();
		if (msg.getHopCount() > 0) {
			Set<ShallowNode> sentNodes = new HashSet<ShallowNode>();
			for (ShallowNode shallowNode : nodeTable) {
				if (!msg.getSourceNode().equals(shallowNode)) {
					sentNodes.add(shallowNode);
					sendMessage(shallowNode, msg);
				}
			}
			System.out.printf("Node %s sent the one-to-all message(%s) from %s to nodes %s\n", self, msg.getText(), msg.getSourceNode(), sentNodes);
		}
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
							if (!json.isEmpty()) {
								Type setType = new TypeToken<HashSet<ShallowNode>>() {}.getType();
								Set<ShallowNode> otherNodes = new Gson().fromJson(json, setType);
								System.out.printf("Node %s received a node table consisting of nodes %s from node %s\n", self, otherNodes, randomNode);
								nodeTable.addAll(otherNodes);
								nodeTable.remove(self);
							}
							trimNodeTable();
							System.out.printf("After merging and trimming the new node table of Node %s looks like %s\n", self, nodeTable);
						} catch (UnknownHostException | ConnectException e) {
							System.out.printf("Node %s failed to send its node table to node %s. Therefore it will be removed from the sender's node table", self, randomNode);
							nodeTable.remove(randomNode); // remove entry from node table if it cannot be reached
							continue; 					  // and pick another entry until no further entry is left
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if(socket != null) socket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		// main thread waits for new connections.
		// once a connection is established, it receives a message from an other node.
		// if it is a one-to-all message with a hopCount > 1, the message is sent to 
		// all nodes in the node table (except the source node of the message).
		// if it is the node table from the other node,
		// it sends its own (unmerged) table including its own information using the same socket connection.
		// 'merges' the both tables,
		// and trims the table to contain a total of n entries
		while(connected) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				String json = recv(socket);
				try {
					Message recMsg = new Gson().fromJson(json, Message.class);
					if (recMsg.getRecName() == null) {			// it's a normal one-to-all message
						handleOneToAllMessageReception(recMsg);
					} else {									// it's a name-resolution message
						handleNameResolutionMessage(recMsg);
					}
				} catch (JsonSyntaxException e) {
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
				}				
			} catch(SocketTimeoutException e) {
				System.out.printf("Node %s socket timed out waiting for new connections. Trying again...\n", self);
				continue;
			} catch (IOException e) { 
				e.printStackTrace();
			} finally {
				try {
					if(socket != null) socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleNameResolutionMessage(Message recMsg) {

		System.out.println("Node " + self.getName() + " received name-resolution message from " + recMsg.getSourceNode().getName());
		
		//requested node was found
		if(recMsg.getTargetNode() != null){
			//check if I am the requester
			if(recMsg.getSourceNode().equals(self)){
				//fill requsted node attribute
				setRequestedNode(recMsg.getTargetNode());
			}
			return;
		}
		
		//I'm the requested node
		if(self.getName().equals(recMsg.getRecName())){
			
			System.out.println("Requested Node " + recMsg.getRecName() + " was found itself");
			
			returnNode(recMsg, self);
			return;
		}
		
		//check if requested node is known
		for (ShallowNode shallowNode : nodeTable) {
			if(shallowNode.getName().equals(recMsg.getRecName())){
				
				System.out.println("Requested Node " + recMsg.getRecName() + " was found by " + self.toString());
				
				returnNode(recMsg, shallowNode);
				return;
			}
		}
		
		//node is not known by me...ask other nodes
		handleOneToAllMessageReception(recMsg);
		
	}

	private void returnNode(Message msg, ShallowNode node) {

		//fill target node in message
		msg.setTargetNode(node);
		
		//return message to requester
		sendMessage(msg.getSourceNode(), msg);
		
	}

	public static void main(String[] args) {
		final int n = 3;
		// creating a network of 3*n nodes
		Node firstPeer = new Node("Node0", n);
		List<Node> peers = new ArrayList<Node>();
		peers.add(firstPeer);
		for(int i = 1; i < 9; i++) {
			Node prev = peers.get(i-1);
			peers.add(new Node("Node"+i, n, new ShallowNode(prev.getIp(), prev.getPort(), prev.getName())));
		}
		
		for(Thread t : peers) {
			t.start();
		}
		
		// wait for 3 node table update cycles before sending a one-to-all message
		try {
			Thread.sleep(11000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// send a one-to-all message from first node
		firstPeer.sendOneToAllMessage("Hello, this is " + firstPeer.getName() + ".");
		
		// wait for 3 node table update cycles before sending a one-to-all message
		try {
			Thread.sleep(11000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		//search for a node in the network
		ShallowNode requestedNode = firstPeer.requestNodeByName("Node4");
		
		if(requestedNode == null){
			System.out.println("The requested node could not be found :-(");
		}else{
			System.out.println("Requested Node was found: " + requestedNode.toString());
		}
		
		
		// every second one node will be disconnected from the p2p network
		for(Node node : peers) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			node.disconnect();
		}
		
		// wait for all threads to be finished
		for(Thread t : peers) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
