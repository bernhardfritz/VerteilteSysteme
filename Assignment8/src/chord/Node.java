package chord;

public class Node implements INode {

	public static final int m = 5;

	private int id;
	private Node successor;
	private Node predecessor;

	public int getId() {
		return id;
	}

	public Node getSuccessor() {
		return successor;
	}

	public void setSuccessor(Node successor) {
		this.successor = successor;
	}

	public Node getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(Node predecessor) {
		this.predecessor = predecessor;
	}

	// create new Node and return reference
	public static Node getNodeInstance(int id) {

		//check valid id
		if (id >= Math.pow(id, m) || id < 0) {
			return null;
		}

		//create new node
		return new Node(id);
	}

	// constructor
	private Node(int id){
		this.id = id;
	}

	@Override
	public void join() {

	}

	@Override
	public void sendMSG(String msg, int target) {

	}

}
