package chord;

public class FingerTableEntry {

	private int start;
	
	private int from;
	
	private int to;
	
	private Node successor;

	public FingerTableEntry(int start, int from, int to, Node successor) {
		this.start = start;
		this.from = from;
		this.to = to;
		this.successor = successor;
	}
	
	@Override
	public String toString() {
		return start + " - [" + start + ", " + to + ") - " + successor.getId();
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public Node getSuccessor() {
		return successor;
	}

	public void setSuccessor(Node successor) {
		this.successor = successor;
	}
}