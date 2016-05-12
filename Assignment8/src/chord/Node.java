package chord;

import java.util.ArrayList;
import java.util.List;

public class Node implements INode {

	private int m;

	private int id;
	
	private Node predecessor;
	
	private List<FingerTableEntry> fingerTable;

	public Node(int id, int m){
		this.id = id;
		this.m = m;
		fingerTable = new ArrayList<FingerTableEntry>(m);
		fillFingerTable();
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
		if (i == 3) {
			System.out.println("asdf");
		}
		int asdf = ((fingerTable.get(i).getSuccessor().getId() <= id) ? (fingerTable.get(i).getSuccessor().getId() + (int)Math.pow(2, m)) : fingerTable.get(i).getSuccessor().getId());
		if (s.getId() > id && s.getId() < asdf) {
			fingerTable.get(i).setSuccessor(s);
			Node p = predecessor;
			p.updateFingerTable(s, i);
		}
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

	@Override
	public void sendMSG(String msg, int target) {

	}
	
	public Node findSuccessor(int id) {
		Node node = findPredecessor(id);
		return node.getSuccessor();
	}

	public Node findPredecessor(int id) {
		Node node = this;
		while (id <= node.getId() || id > ((node.getSuccessor().getId() < node.getId()) ? node.getSuccessor().getId() + Math.pow(2, m) : node.getSuccessor().getId())) {
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
			int nodeId = (fingerTable.get(i).getSuccessor().getId() < this.getId()) ? (fingerTable.get(i).getSuccessor().getId() + (int)Math.pow(2, m)) : fingerTable.get(i).getSuccessor().getId();
			int asdf = ((id < this.getId()) ? (id + (int)Math.pow(2, m)) : id);
			if (nodeId > this.getId() && nodeId < asdf) {
				return fingerTable.get(i).getSuccessor();
			}
		}
		
		return this;
	}
	
	public void printFingerTable() {
		System.out.println("Node" + id + ":");
		for (FingerTableEntry finger : fingerTable) {
			System.out.println(finger);
		}
	}
}
