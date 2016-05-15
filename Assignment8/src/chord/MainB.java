package chord;

public class MainB {

	public static void main(String[] args) {
		int m = 5;
		
		Node n1 = new Node(1, m);
		Node n3 = new Node(3, m);
		Node n7 = new Node(7, m);
		Node n8 = new Node(8, m);
		Node n12 = new Node(12, m);
		Node n15 = new Node(15, m);
		Node n19 = new Node(19, m);
		Node n25 = new Node(25, m);
		Node n27 = new Node(27, m);
		
		n1.join(null);
		n3.join(n1);
		n7.join(n1);
		n8.join(n1);
		n12.join(n1);
		n15.join(n1);
		n19.join(n1);
		n25.join(n1);
		n27.join(n1);
		
		n1.printFingerTable();
		System.out.println("Predecessor: " + n1.getPredecessor() + "\n");
		n3.printFingerTable();
		System.out.println("Predecessor: " + n3.getPredecessor() + "\n");
		n7.printFingerTable();
		System.out.println("Predecessor: " + n7.getPredecessor() + "\n");
		n8.printFingerTable();
		System.out.println("Predecessor: " + n8.getPredecessor() + "\n");
		n12.printFingerTable();
		System.out.println("Predecessor: " + n12.getPredecessor() + "\n");
		n15.printFingerTable();
		System.out.println("Predecessor: " + n15.getPredecessor() + "\n");
		n19.printFingerTable();
		System.out.println("Predecessor: " + n19.getPredecessor() + "\n");
		n25.printFingerTable();
		System.out.println("Predecessor: " + n25.getPredecessor() + "\n");
		n27.printFingerTable();
		System.out.println("Predecessor: " + n27.getPredecessor() + "\n");
		
		n25.sendMSG("Hello, this is " + n25 + ".", n8);
	}
}