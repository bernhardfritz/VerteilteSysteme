package multithreaded;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

public class RequestHandler implements Runnable {

	private Protocol protocol;
	List<String> database;
	private Socket socket;
	
	public RequestHandler(Protocol protocol, List<String> database, Socket socket){
		this.protocol = protocol;
		this.database = database;
		this.socket   = socket;
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
	
	//shutdown server
	private void shutdown(String username){
		
		//only admin can shut down server
		if(username.equals("admin") == false){
			System.out.println("Shutdown can only be performed by an admin!");
		//shutdown server
		} else {
			Server.shutdown();
		}
	}
	
	@Override
	public void run() {
		
		Gson gson = new Gson();
		
		//short delay - for demonstration purpose
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
        	// process request according to protocol
            String jsonAuthReq = protocol.replay(socket);
            // Unwrap JSON request
			AuthenticationRequest authReq = gson.fromJson(jsonAuthReq, AuthenticationRequest.class);
			System.out.printf("%s trying to authenticate...\n", authReq.getUsername());
			
			// Check if username is registered
			if(database.contains(authReq.getUsername())) {
				System.out.printf("%s has successfully been authenticated!\n", authReq.getUsername());
				
				// Create JSON authentication success response
				AuthenticationResponse authRes = new AuthenticationResponse(true);
				String jsonAuthRes = gson.toJson(authRes);
				// Send response using protocol
				protocol.respond(socket, jsonAuthRes);
				// Receive request using protocol
				String jsonOpReq = protocol.replay(socket);
				// Unwrap JSON request
				OperationRequest opReq = gson.fromJson(jsonOpReq, OperationRequest.class);
				System.out.printf("%s requesting result of %s %s\n", authReq.getUsername(), opReq.getOperation(), Arrays.toString(opReq.getOperators()));
				System.out.printf("Calculating %s request...\n", authReq.getUsername());
				int result = 0;
				switch(opReq.getOperation()) {
					case ADDITION: result = addition(opReq.getOperators()); break;
					case SUBTRACTION: result = subtraction(opReq.getOperators()); break;
					case MULTIPLICATION: result = multiplication(opReq.getOperators()); break;
					case LUCAS: result = lucas(opReq.getOperators()); break;
					case SHUTDOWN: shutdown(authReq.getUsername()); break;
				}
				System.out.printf("Returning result(%d) to %s\n", result, authReq.getUsername());
				// Create JSON operation response
				OperationResponse opRes = new OperationResponse(result);
				String jsonOpRes = gson.toJson(opRes);
				// Send response using protocol
				protocol.respond(socket, jsonOpRes);
			} else {
				System.out.printf("%s couldn't be authenticated!\n", authReq.getUsername());
				// Create JSON authentication response
				AuthenticationResponse authRes = new AuthenticationResponse(false);
				String jsonAuthRes = gson.toJson(authRes);
				// Send response using protocol
				protocol.respond(socket, jsonAuthRes);
			}
        } finally {
        	System.out.println("Closing connection...");
            try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
	}

}
