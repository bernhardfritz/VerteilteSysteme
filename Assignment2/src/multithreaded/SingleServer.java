package multithreaded;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import singlethreaded.AuthenticationRequest;
import singlethreaded.AuthenticationResponse;
import singlethreaded.OperationRequest;
import singlethreaded.OperationResponse;
import singlethreaded.Protocol;

public class SingleServer {
	private Protocol protocol;
	List<String> database;
	private volatile boolean shutdown;
	
	public SingleServer(Protocol protocol) {
		this.protocol = protocol;
		database = new ArrayList<String>();
		for (int i = 0; i < 10000; i++) {
			if ((i % 3) == 0) {
				database.add("Client" + i);
			}
		}
		shutdown = false;
	}
	
	private int addition(int... operators) {
		return operators[0] + operators[1];
	}
	
	private int subtraction(int... operators) {
		return operators[0] - operators[1];
	}
	
	private int multiplication(int... operators) {
		return operators[0] * operators[1];
	}
	
	//		 {	2				if n = 0;
	// L_n = {	1				if n = 1;
	//		 {	L_n-1 + L_n-2	if n > 1.
	private int lucas(int... operators) {
		if(operators[0] == 0) return 2;
		if(operators[0] == 1) return 1;
		else return lucas(operators[0] - 1) + lucas(operators[0] - 2);
	}
	
	public void listen() {
		// Open socket
		ServerSocket listener = null;
		Gson gson = new Gson();
		try {
			listener = new ServerSocket(protocol.port);
			System.out.println("Server started! Listening on 127.0.0.1:9090");
            while (!shutdown) {
            	System.out.println("Waiting for incoming connections...");
        		// wait for client connections
                Socket socket = listener.accept();
                try {
                	
    				//sleep - to demonstrate single thread
    				try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	
                	// process request according to protocol
                    String jsonAuthReq = protocol.replay(socket);
                    // Unwrap JSON request
        			AuthenticationRequest authReq = gson.fromJson(jsonAuthReq, AuthenticationRequest.class);
        			System.out.printf("Client(%s) trying to authenticate...\n", authReq.getUsername());
        			// Check if username is registered
        			if(database.contains(authReq.getUsername())) {
        				System.out.printf("Client(%s) has successfully been authenticated!\n", authReq.getUsername());
        				// Create JSON authentication success response
        				AuthenticationResponse authRes = new AuthenticationResponse(true);
        				String jsonAuthRes = gson.toJson(authRes);
        				// Send response using protocol
        				protocol.respond(socket, jsonAuthRes);
        				// Receive request using protocol
        				String jsonOpReq = protocol.replay(socket);
        				// Unwrap JSON request
        				OperationRequest opReq = gson.fromJson(jsonOpReq, OperationRequest.class);
        				System.out.printf("Client(%s) requesting result of %s %s\n", authReq.getUsername(), opReq.getOperation(), Arrays.toString(opReq.getOperators()));
        				System.out.printf("Calculating Client(%s) request...\n", authReq.getUsername());
        				int result = 0;
        				switch(opReq.getOperation()) {
        					case ADDITION: result = addition(opReq.getOperators()); break;
        					case SUBTRACTION: result = subtraction(opReq.getOperators()); break;
        					case MULTIPLICATION: result = multiplication(opReq.getOperators()); break;
        					case LUCAS: result = lucas(opReq.getOperators()); break;
        				}
        				System.out.printf("Returning result(%d) to Client(%s)\n", result, authReq.getUsername());
        				// Create JSON operation response
        				OperationResponse opRes = new OperationResponse(result);
        				String jsonOpRes = gson.toJson(opRes);        	
        				// Send response using protocol
        				protocol.respond(socket, jsonOpRes);
        			} else {
        				System.out.printf("Client(%s) couldn't be authenticated!\n", authReq.getUsername());
        				// Create JSON authentication response
        				AuthenticationResponse authRes = new AuthenticationResponse(false);
        				String jsonAuthRes = gson.toJson(authRes);
        				// Send response using protocol
        				protocol.respond(socket, jsonAuthRes);
        			}
                } finally {
                	System.out.println("Closing connection...");
                    socket.close();
                }
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
	public void shutdown()  {
		shutdown = true;
		System.out.println("Shutting down...");
	}
	
	public static void main(String[] args) {
		System.out.println("Starting server...");
		SingleServer server = new SingleServer(new Protocol(9090));
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        server.shutdown();
		    }
		});
		server.listen();
	}
}
