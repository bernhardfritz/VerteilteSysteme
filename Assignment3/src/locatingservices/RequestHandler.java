package locatingservices;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.google.gson.Gson;

public class RequestHandler implements Runnable {
	private long threadID;
	private Protocol protocol;
	private Socket socket;
	
	public RequestHandler(long threadID, Protocol protocol, Socket socket){
		this.threadID = threadID;
		this.protocol = protocol;
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
	
	@Override
	public void run() {
		Gson gson = new Gson();

        try {
			// Receive request using protocol
			String jsonOpReq = protocol.replay(socket);
			// Unwrap JSON request
			OperationRequest opReq = gson.fromJson(jsonOpReq, OperationRequest.class);
			System.out.printf("%d: Client requesting result of %s %s\n", threadID, opReq.getOperation(), Arrays.toString(opReq.getOperators()));
			System.out.println(threadID + ": Calculating request...");
			int result = 0;
			switch(opReq.getOperation()) {
				case "ADDITION": result = addition(opReq.getOperators()); break;
				case "SUBTRACTION": result = subtraction(opReq.getOperators()); break;
				case "MULTIPLICATION": result = multiplication(opReq.getOperators()); break;
				case "LUCAS": result = lucas(opReq.getOperators()); break;
			}
			System.out.printf("%d: Returning result(%d).\n", threadID, result);
			// Create JSON operation response
			OperationResponse opRes = new OperationResponse(result);
			String jsonOpRes = gson.toJson(opRes);
			// Send response using protocol
			protocol.respond(socket, jsonOpRes);
        } finally {
        	System.out.println(threadID + ": Closing connection...");
            try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}