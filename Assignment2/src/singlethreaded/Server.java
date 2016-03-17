package singlethreaded;

public class Server {
	private Protocol protocol;
	
	public Server(Protocol protocol) {
		this.protocol = protocol;
	}
	
	private int addition(int a, int b) {
		return a + b;
	}
	
	private int subtraction(int a, int b) {
		return a - b;
	}
	
	private int multiplication(int a, int b) {
		return a * b;
	}
	
	//		 {	2				if n = 0;
	// L_n = {	1				if n = 1;
	//		 {	L_n-1 + L_n-2	if n > 1.
	private int lucas(int n) {
		if(n == 0) return 2;
		if(n == 1) return 1;
		else return lucas(n - 1) + lucas(n - 2);
	}
	
	public void listen() {
		// Open socket
		// wait for client connections
		// process client requests according to protocol by forwarding it to the replay method of the protocol class
	}
	
	public static void main(String[] args) {
		Server server = new Server(new Protocol(1000));
		server.listen();
	}
}
