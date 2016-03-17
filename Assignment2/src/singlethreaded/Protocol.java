package singlethreaded;

import java.net.Socket;

public class Protocol {
	public final int port;
	
	public Protocol(int port) {
		this.port = port;
	}
	
	public String request(Socket socket, String jsonReq) {
		String jsonRes = "";
		// use input / output streams to realize the client side of the communication
		return jsonRes;
	}
	
	public void replay(Socket socket) {
		// conduct the server's end of the communcation protocol
	}
	
	/*
	 * JSON operation request/response example.
	 * Client request:
	 * {
	 *   "operation": "ADDITION",
	 *   "operators": [1, 2]
	 * }
	 * 
	 * Server response:
	 * {
	 *   result: 3
	 * }
	 */
	
	/*
	 * JSON authentication request/response example.
	 * Client request:
	 * {
	 *   "username": "foo"
	 * }
	 * 
	 * Server response:
	 * {
	 *   "success": true
	 * }
	 */
}
