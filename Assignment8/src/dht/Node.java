package dht;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Node implements INode {

	private int m;

	private int id;
	
	private Node predecessor;
	
	private Map<Integer, MarshalledObject<Object>> map;
	
	private List<FingerTableEntry> fingerTable;

	public Node(int id, int m){
		this.id = id;
		this.m = m;
		fingerTable = new ArrayList<FingerTableEntry>(m);
		fillFingerTable();
		this.map = new HashMap<Integer, MarshalledObject<Object>>();
	}
	
	@Override
	public void join(Node node) {
		// first node in chord network
		if (node == null) {
			for (int i = 1; i <= m; i++) {
				fingerTable.get(i-1).setSuccessor(this);
			}
			predecessor = this;
		} else {
			initFingerTable(node);
			updateOthers();
		}
	}

	/**
	 * Send a message to the target node recursively.
	 */
	@Override
	public void sendMSG(String msg, Node target) {
		sendMSG(msg, target, this, 0);
	}
	
	public void sendMSG(String msg, Node target, Node source, int hopCount) {		
		if(target.getId() != id) {
			Node node = closestPrecedingFinger(target.getId());
			int i = 0;
			for (FingerTableEntry entry : fingerTable) {
				if (node.equals(entry.getSuccessor())) {
					i = fingerTable.indexOf(entry);
				}
			}
			
			if (getSuccessor().equals(target)) {
				node = getSuccessor();
				i = 0;
			}
			System.out.printf("%s: Send message from %s to %s(i=%d)\n", this, source, node, i+1);
			node.sendMSG(msg, target, source, ++hopCount);
		} else {
			System.out.printf("%s: Received message from %s(hopcount=%d): \"%s\"\n", this, source, hopCount, msg);
		}
	}
	
	private void fillFingerTable() {
		for (int i = 1; i <= m; i++) {
			int start = ((int) (id + Math.pow(2, i-1))) % ((int) Math.pow(2, m));
			int to = ((int) (id + Math.pow(2, i))) % ((int) Math.pow(2, m));
			
			fingerTable.add(new FingerTableEntry(start, start, to, null));
		}
	}
	
	public void initFingerTable(Node node) {
		fingerTable.get(0).setSuccessor(node.findSuccessor(fingerTable.get(0).getStart()));
		
		Node successor = fingerTable.get(0).getSuccessor();
		predecessor = successor.getPredecessor();
		successor.setPredecessor(this);
		
		for (int i = 0; i < m-1; i++) {
			if (fingerTable.get(i+1).getStart() >= id && fingerTable.get(i+1).getStart() < fingerTable.get(i).getSuccessor().getId()) {
				fingerTable.get(i+1).setSuccessor(fingerTable.get(i).getSuccessor());
			} else {
				fingerTable.get(i+1).setSuccessor(node.findSuccessor(fingerTable.get(i+1).getStart()));
			}
		}
	}
	
	public void updateOthers() {
		predecessor.updateFingerTable(this, 0);
		for (int i = 0; i < m-1; i++) {
			Node p = findPredecessor((id - (int)Math.pow(2, i)) % ((int) Math.pow(2, m)));
			p.updateFingerTable(this, i+1);
		}
	}
	
	public void updateFingerTable(Node s, int i) {
		int upperBound = correctUpperBound(id, fingerTable.get(i).getSuccessor().getId());
		if (fingerTable.get(i).getSuccessor().getId() == id) {
			if (fingerTable.get(i).getStart() <= s.getId()) {
				upperBound += Math.pow(2, m);
			}
		}
		if (s.getId() > id && s.getId() < upperBound) {
			if (fingerTable.get(i).getStart() <= s.getId()) {
				fingerTable.get(i).setSuccessor(s);
			}
			Node p = predecessor;
			p.updateFingerTable(s, i);
		}
	}

	public Node findSuccessor(int id) {
		if (this.getId() == id) {
			return this;
		}
		Node node = findPredecessor(id);
		return node.getSuccessor();
	}

	public Node findPredecessor(int id) {
		Node node = this;
		while (id <= node.getId() || id > correctUpperBound(node.getId(), node.getSuccessor().getId())) {
			if (node.getId() == node.getSuccessor().getId()) {
				return node;
			}
			Node oldNode = node;
			node = node.closestPrecedingFinger(id);
			if (node.getId() == oldNode.getId()) {
				return node;
			}
		}
		
		return node;
	}
	
	public Node closestPrecedingFinger(int id) {
		for (int i = m-1; i >= 0; i--) {
			int nodeId = correctUpperBound(this.getId(), fingerTable.get(i).getSuccessor().getId());
			int asdf = ((id < this.getId()) ? (id + (int)Math.pow(2, m)) : id);
			if (nodeId > this.getId() && nodeId < asdf) {
				return fingerTable.get(i).getSuccessor();
			}
		}
		
		return this;
	}
	
	private int correctUpperBound(int lo, int hi) {
		if (hi < lo) {
			return hi + (int)Math.pow(2, m);
		}
		return hi;
	}
	
	public void printFingerTable() {
		System.out.println("Node" + id + ":");
		for (FingerTableEntry finger : fingerTable) {
			System.out.println(finger);
		}
	}
	
	public void printMap() {
		StringBuilder sb = new StringBuilder(this + ": Map{");
		for (Entry<Integer, MarshalledObject<Object>> entry : map.entrySet()) {
			try {
				sb.append(entry.getKey() + "=" + entry.getValue().get() + ", ");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!map.isEmpty()) {
			sb.replace(sb.length()-2, sb.length(), "");
		}
		sb.append("}");
		System.out.println(sb.toString());
	}
	
	public MarshalledObject<Object> put(Integer key, MarshalledObject<Object> value) {
		return map.put(key, value);
	}
	
	public MarshalledObject<Object> get(Integer key) {
		return map.get(key);
	}
	
	public boolean contains(Integer key) {
		return map.containsKey(key);
	}
	
	public MarshalledObject<Object> remove(Integer key) {
		return map.remove(key);
	}
	
	@Override
	public String toString() {
		return "Node" + id;
	}
	
	@Override
	public boolean equals(Object obj) {
		Node that = (Node)obj;
		return this.getId() == that.getId();
	}
	
	@Override
	public int hashCode() {
		return new Integer(id).hashCode();
	}
	
	public int getId() {
		return id;
	}

	public Node getSuccessor() {
		return fingerTable.get(0).getSuccessor();
	}

	public void setSuccessor(Node successor) {
		fingerTable.get(0).setSuccessor(successor);
	}

	public Node getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(Node predecessor) {
		this.predecessor = predecessor;
	}
}