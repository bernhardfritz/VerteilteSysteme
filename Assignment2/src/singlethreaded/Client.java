package singlethreaded;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.google.gson.Gson;

public class Client {
	private Protocol protocol;
	
	public Client(Protocol protocol) {
		this.protocol = protocol;
	}
	
	public int handleRequest(String ip, String username, Operation operation, int... operators) {
		int result = 0;
		Gson gson = new Gson();
		try {
			System.out.printf("Connecting to server %s:%d...\n", ip, protocol.port);
			// Open connection to server
			Socket socket = new Socket(ip, protocol.port);
			try {
				System.out.println("Connected!");
				// Create JSON authentication request using parameter username
				AuthenticationRequest authReq = new AuthenticationRequest(username);
				String jsonAuthReq = gson.toJson(authReq);
				System.out.printf("Trying to authenticate with username: %s\n", username);
				// Use protocol's request method to send request
				String jsonAuthRes = protocol.request(socket, jsonAuthReq);
				// Unwrap JSON response
				AuthenticationResponse authRes = gson.fromJson(jsonAuthRes, AuthenticationResponse.class);
				// If authentication successful
				if(authRes.isSuccess()) {
					System.out.println("Successfully authenticated!");
					// Create JSON operation request using parameters operation and operators
					OperationRequest opReq = new OperationRequest(operation, operators);
					String jsonOpReq = gson.toJson(opReq);
					System.out.printf("Sending operation request %s %s...\n", operation, Arrays.toString(operators));
					// Use protocol's request method to send request
					String jsonOpRes = protocol.request(socket, jsonOpReq);
					// Unwrap JSON response
					OperationResponse opRes = gson.fromJson(jsonOpRes, OperationResponse.class);
					result = opRes.getResult();
					System.out.printf("Received result: %s\n", result);
				} else {
					System.out.println("Authentication failed!");
				}
			} finally {
				System.out.println("Closing connection...");
				// finally close connection
				socket.close();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	public static void main(String[] args) {
		Client client = new Client(new Protocol(9090));
		client.handleRequest("127.0.0.1", "foo", Operation.LUCAS, 10);
		client.handleRequest("127.0.0.1", "foo", Operation.ADDITION, 1, 2);
		client.handleRequest("127.0.0.1", "asdf", Operation.SUBTRACTION, 2, 1);
	}
}
