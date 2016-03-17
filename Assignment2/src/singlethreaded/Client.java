package singlethreaded;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
			// Open connection to server
			Socket socket = new Socket(ip, protocol.port);
			// Create JSON authentication request using parameter username
			AuthenticationRequest authReq = new AuthenticationRequest(username);
			String jsonAuthReq = gson.toJson(authReq);
			// Use protocol's request method to send request
			protocol.request(socket, jsonAuthReq);
			String jsonAuthRes = "";
			// Unwrap JSON response
			AuthenticationResponse authRes = gson.fromJson(jsonAuthRes, AuthenticationResponse.class);
			// If authentication successful
			if(authRes.isSuccess()) {
				// Create JSON operation request using parameters operation and operators
				OperationRequest opReq = new OperationRequest(operation, operators);
				String jsonOpReq = gson.toJson(opReq);
				// Use protocol's request method to send request
				protocol.request(socket, jsonOpReq);
				String jsonOpRes = "";
				// Unwrap JSON response
				OperationResponse opRes = gson.fromJson(jsonOpRes, OperationResponse.class);
				result = opRes.getResult();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// finally close connections
		}
		return result;
	}
	public static void main(String[] args) {
		Client client = new Client(new Protocol(1000));
		client.handleRequest("127.0.0.1", "foo", Operation.LUCAS, 42);
	}
}
