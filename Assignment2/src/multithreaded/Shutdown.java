package multithreaded;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.google.gson.Gson;

public class Shutdown {
	private Protocol protocol;

	public Shutdown(Protocol protocol) {
		this.protocol = protocol;
	}

	public int handleRequest(String ip, String username, Operation operation, int... operators) {
		int result = 0;
		Gson gson = new Gson();
		try {
			System.out.printf("%s: Connecting to server %s:%d...\n", username, ip, protocol.port);
			// Open connection to server
			Socket socket = new Socket(ip, protocol.port);
			try {
				System.out.printf("%s: Connected!\n", username);
				// Create JSON authentication request using parameter username
				AuthenticationRequest authReq = new AuthenticationRequest(username);
				String jsonAuthReq = gson.toJson(authReq);
				System.out.printf("Trying to authenticate with username: %s\n", username);
				// Use protocol's request method to send request
				String jsonAuthRes = protocol.request(socket, jsonAuthReq);
				// Unwrap JSON response
				AuthenticationResponse authRes = gson.fromJson(jsonAuthRes, AuthenticationResponse.class);
				// If authentication successful
				if (authRes.isSuccess()) {
					System.out.printf("%s: Successfully authenticated!\n", username);
					// Create JSON operation request using parameters operation
					// and operators
					OperationRequest opReq = new OperationRequest(operation, operators);
					String jsonOpReq = gson.toJson(opReq);
					System.out.printf("%s: Sending operation request %s %s...\n", username, operation,
							Arrays.toString(operators));
					// Use protocol's request method to send request
					String jsonOpRes = protocol.request(socket, jsonOpReq);
					// Unwrap JSON response
					OperationResponse opRes = gson.fromJson(jsonOpRes, OperationResponse.class);
					result = opRes.getResult();
					System.out.printf("%s: Received result: %s\n", username, result);
				} else {
					System.out.printf("%s: Authentication failed!\n", username);
				}
			} finally {
				System.out.printf("%s: Closing connection...\n", username);
				// finally close connection
				socket.close();
			}

		} catch (SocketException e) {
			System.out.println("Socket was shut down!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {

		//create admin
		Shutdown admin = new Shutdown(new Protocol(9090));

		//send shutdown request
		admin.handleRequest("127.0.0.1", "admin", Operation.SHUTDOWN);
	}
}
