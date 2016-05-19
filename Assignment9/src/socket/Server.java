package socket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

public class Server {
	private Protocol protocol;
	List<String> database;
	private volatile boolean shutdown;
	
	public Server(Protocol protocol) {
		this.protocol = protocol;
		database = new ArrayList<String>();
		database.add("foo");
		shutdown = false;
	}
	
	private BigInteger addition(BigInteger... operators) {
		return operators[0].add(operators[1]);
	}
	
	public void listen() {
		// Open socket
		ServerSocket listener = null;
		Gson gson = new Gson();
		try {
			listener = new ServerSocket(protocol.port);
			//.println("Server started! Listening on 127.0.0.1:9090");
            while (!shutdown) {
            	//.println("Waiting for incoming connections...");
        		// wait for client connections
                Socket socket = listener.accept();
                try {
    				// Receive request using protocol
    				String jsonOpReq = protocol.replay(socket);
    				// Unwrap JSON request
    				OperationRequest opReq = gson.fromJson(jsonOpReq, OperationRequest.class);
    				//.printf("Client requesting result of %s %s\n", opReq.getOperation(), Arrays.toString(opReq.getOperators()));
    				//.printf("Calculating Client request...\n");
    				
    				BigInteger result = BigInteger.ZERO;
    				switch(opReq.getOperation()) {
    					case ADDITION: result = addition(opReq.getOperators()); break;
    					case SHUTDOWN: shutdown();
    				}
    				
    				//.printf("Returning result(%d) to Client\n", result);
    				// Create JSON operation response
    				OperationResponse opRes = new OperationResponse(result);
    				String jsonOpRes = gson.toJson(opRes);
    				// Send response using protocol
    				protocol.respond(socket, jsonOpRes);
                } finally {
                	//.println("Closing connection...");
                    socket.close();
                }
            }
		} catch (IOException e) {
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
		//.println("Shutting down...");
	}
	
	public static void main(String[] args) {
		//.println("Starting server...");
		Server server = new Server(new Protocol(9090));
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        server.shutdown();
		    }
		});
		server.listen();
	}
}
