package socket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;

public class Server {
	private Protocol protocol;
	private volatile boolean shutdown;
	
	public Server(Protocol protocol) {
		this.protocol = protocol;
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
            while (!shutdown) {
        		// wait for client connections
                Socket socket = listener.accept();
                try {
    				// Receive request using protocol
    				String jsonOpReq = protocol.replay(socket);
    				// Unwrap JSON request
    				OperationRequest opReq = gson.fromJson(jsonOpReq, OperationRequest.class);
    				
    				BigInteger result = BigInteger.ZERO;
    				switch(opReq.getOperation()) {
    					case ADDITION: result = addition(opReq.getOperators()); break;
    					case SHUTDOWN: shutdown();
    				}
    				
    				// Create JSON operation response
    				OperationResponse opRes = new OperationResponse(result);
    				String jsonOpRes = gson.toJson(opRes);
    				// Send response using protocol
    				protocol.respond(socket, jsonOpRes);
                } finally {
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
	}
	
	public static void main(String[] args) {
		final Server server = new Server(new Protocol(9090));
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        server.shutdown();
		    }
		});
		server.listen();
	}
}