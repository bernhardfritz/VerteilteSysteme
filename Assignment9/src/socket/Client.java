package socket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import com.google.gson.Gson;

public class Client {
	private Protocol protocol;
	
	public Client(Protocol protocol) {
		this.protocol = protocol;
	}
	
	public BigInteger handleRequest(String ip, Operation operation, BigInteger... operators) {
		BigInteger result = BigInteger.ZERO;
		Gson gson = new Gson();
		try {
			// Open connection to server
			Socket socket = new Socket(ip, protocol.port);
			try {
				// Create JSON operation request using parameters operation and operators
				OperationRequest opReq = new OperationRequest(operation, operators);
				String jsonOpReq = gson.toJson(opReq);
				// Use protocol's request method to send request
				String jsonOpRes = protocol.request(socket, jsonOpReq);
				// Unwrap JSON response
				OperationResponse opRes = gson.fromJson(jsonOpRes, OperationResponse.class);
				result = opRes.getResult();
			} finally {
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
		Random r = new Random(0);
		long start = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {
			client.handleRequest("127.0.0.1", Operation.ADDITION, BigInteger.valueOf(r.nextLong()), BigInteger.valueOf(r.nextLong()));
		}
		long end = System.currentTimeMillis();
		double duration = (end - start) / 1000.0; // in ms
		System.out.println("[Socket] Duration: " + duration + " ms");
		
		client.handleRequest("127.0.0.1",  Operation.SHUTDOWN);
	}
}
