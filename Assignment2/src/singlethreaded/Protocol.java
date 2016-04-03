package singlethreaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Protocol {
	public final int port;
	
	public Protocol(int port) {
		this.port = port;
	}
	
	// sends request jsonReq and returns response jsonRes to caller
	public String request(Socket socket, String jsonReq) {
		String jsonRes = "";
		try {
			// use input / output streams to realize the client side of the communication
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(jsonReq);
			jsonRes = in.readLine();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return jsonRes;
	}
	
	// waits for incoming request and returns content to caller
	public String replay(Socket socket) {
		String jsonReq = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			jsonReq = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonReq;
	}
	
	// sends response jsonRes and returns to caller
	public void respond(Socket socket, String jsonRes) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(jsonRes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
	 *   "result": 3
	 * }
	 */
}
